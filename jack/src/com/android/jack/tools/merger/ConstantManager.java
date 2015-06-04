/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.jack.tools.merger;

import com.android.jack.Jack;
import com.android.jack.dx.dex.DexFormat;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.FieldId;
import com.android.jack.dx.io.MethodId;
import com.android.jack.dx.io.ProtoId;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstNat;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A tool to manage constants during dex merging.
 */
public class ConstantManager extends MergerTools {

  public static final int DEFAULT_MULTIDEX_MAIN_DEX_INDEX = 0;

  public static final int DEFAULT_MULTIDEX_NON_MAIN_DEX_INDEX = 1;

  public static final int FIRST_DETERMINISTIC_MODE_INDEX = 0;

  private static final int SLOW_PATH_PERMITS = 10000;

  private int pesimisticFieldsSize = 0;

  private int pesimisticMethodsSize = 0;

  private int pesimisticTypesSize = 0;

  private final boolean bestMergingAccuracy;

  @CheckForNull
  private Door door;

  @CheckForNull
  private volatile MergingOverflowException thrown = null;

  @Nonnull
  private final Semaphore lock = new Semaphore(SLOW_PATH_PERMITS);

  @Nonnull
  private final Map<String, CstString> string2CstStrings =
    new ConcurrentHashMap<String, CstString>();

  @Nonnull
  private final Set<CstFieldRef> cstFieldRefs =
    Collections.newSetFromMap(new ConcurrentHashMap<CstFieldRef, Boolean>());

  @Nonnull
  private final Set<CstMethodRef> cstMethodRefs =
    Collections.newSetFromMap(new ConcurrentHashMap<CstMethodRef, Boolean>());

  @Nonnull
  private final Set<CstType> cstTypes =
    Collections.newSetFromMap(new ConcurrentHashMap<CstType, Boolean>());

  @Nonnull
  private final Map<String, CstString> protoStr2CstString =
    new ConcurrentHashMap<String, CstString>();

  @Nonnull
  private final List<CstIndexMap> cstIndexMaps =
    Collections.synchronizedList(new ArrayList<CstIndexMap>());

  @Nonnull
  public Collection<CstString> getCstStrings() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(string2CstStrings.values());
  }

  @Nonnull
  public Collection<CstFieldRef> getCstFieldRefs() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(cstFieldRefs);
  }

  @Nonnull
  public Collection<CstMethodRef> getCstMethodRefs() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(cstMethodRefs);
  }

  @Nonnull
  public Collection<CstType> getCstTypes() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(cstTypes);
  }

  private ConstantManager(boolean bestMergingAccuracy) {
    this.bestMergingAccuracy = bestMergingAccuracy;
  }

  @Nonnull
  public static ConstantManager getDefaultInstance(boolean bestMergingAccuracy) {
    ConstantManager manager = new ConstantManager(bestMergingAccuracy);
    manager.door = manager.new DefaultDoor(0);
    return manager;
  }

  @Nonnull
  public static ConstantManager getDefaultInstance(boolean bestMergingAccuracy,
      int numberOfMainDexTypesExpected) {
    ConstantManager manager = new ConstantManager(bestMergingAccuracy);
    manager.door = manager.new DefaultDoor(numberOfMainDexTypesExpected);
    return manager;
  }

  @Nonnull
  public static ConstantManager getDeterministicInstance(boolean bestMergingAccuracy,
      int firstTypeIndex) {
    ConstantManager manager = new ConstantManager(bestMergingAccuracy);
    manager.door = manager.new StrictOrderDoor(firstTypeIndex);
    return manager;
  }

  @Nonnull
  private CstIndexMap addDexFileInternal(@Nonnull DexBuffer dexBuffer, boolean fastPath, int index)
      throws MergingOverflowException {
    CstIndexMap cstIndexMap = new CstIndexMap(dexBuffer);

    List<String> cstStringsNewlyAdded = new ArrayList<String>();
    List<CstFieldRef> cstFieldRefsNewlyAdded = new ArrayList<CstFieldRef>();
    List<CstMethodRef> cstMethodRefsNewlyAdded = new ArrayList<CstMethodRef>();
    List<CstType> cstTypesNewlyAdded = new ArrayList<CstType>();

    int idx = 0;
    for (String string : dexBuffer.strings()) {
      CstString cstString = string2CstStrings.get(string);
      if (cstString == null) {
        cstString = new CstString(string);
        string2CstStrings.put(string, cstString);
        cstStringsNewlyAdded.add(string);
      }
      cstIndexMap.addStringMapping(idx++, cstString);
    }

    idx = 0;
    List<String> typeNames = dexBuffer.typeNames();
    for (String typeNameDesc : typeNames) {
      /*
       * Note: VOID isn't put in the intern table of type, since it's special and shouldn't be found
       * by a normal call to intern() from Type.
       */
      CstType cstType = null;
      if (typeNameDesc.equals(Type.VOID.getDescriptor())) {
        cstType = CstType.intern(Type.VOID);
      } else {
        cstType = CstType.intern(Type.intern(typeNameDesc));
      }

      if (cstTypes.add(cstType)) {
        cstTypesNewlyAdded.add(cstType);
      }

      cstIndexMap.addTypeMapping(idx++, cstType);
    }


    idx = 0;
    for (FieldId fieldId : dexBuffer.fieldIds()) {
      CstNat fieldNat = new CstNat(cstIndexMap.getCstString(fieldId.getNameIndex()),
          cstIndexMap.getCstType(fieldId.getTypeIndex()).getDescriptor());
      CstFieldRef cstFieldRef =
          new CstFieldRef(cstIndexMap.getCstType(fieldId.getDeclaringClassIndex()), fieldNat);
      if (cstFieldRefs.add(cstFieldRef)) {
        cstFieldRefsNewlyAdded.add(cstFieldRef);
      }
      cstIndexMap.addFieldMapping(idx++, cstFieldRef);
    }

    idx = 0;
    List<ProtoId> protoIds = dexBuffer.protoIds();
    String[] protoIdx2String = new String[protoIds.size()];

    for (MethodId methodId : dexBuffer.methodIds()) {
      int protoIdx = methodId.getProtoIndex();
      String protoStr = protoIdx2String[protoIdx];
      ProtoId protoId = protoIds.get(protoIdx);

      if (protoStr == null) {
        protoStr = dexBuffer.readTypeList(protoId.getParametersOffset()).toString();
        protoIdx2String[protoIdx] = protoStr;
      }

      protoStr += typeNames.get(protoId.getReturnTypeIndex());

      CstString protoCstString = protoStr2CstString.get(protoStr);
      if (protoCstString == null) {
        protoCstString = new CstString(protoStr);
        protoStr2CstString.put(protoStr, protoCstString);
      }

      CstNat methNat =
          new CstNat(cstIndexMap.getCstString(methodId.getNameIndex()), protoCstString);
      CstMethodRef cstMethodRef =
          new CstMethodRef(cstIndexMap.getCstType(methodId.getDeclaringClassIndex()), methNat);
      if (cstMethodRefs.add(cstMethodRef)) {
        cstMethodRefsNewlyAdded.add(cstMethodRef);
      }
      cstIndexMap.addMethodMapping(idx++, cstMethodRef);
    }

    if (fastPath) {
      synchronized (this) {
        //adjust pessimistic estimations to reflect what really happened
        pesimisticFieldsSize += cstFieldRefsNewlyAdded.size() - dexBuffer.fieldIds().size();
        pesimisticMethodsSize += cstMethodRefsNewlyAdded.size() - dexBuffer.methodIds().size();
        pesimisticTypesSize += cstTypesNewlyAdded.size() - dexBuffer.typeIds().size();
      }
    } else {
      if ((cstFieldRefs.size()) > DexFormat.MAX_MEMBER_IDX + 1) {
        removeItems(cstStringsNewlyAdded, cstFieldRefsNewlyAdded, cstMethodRefsNewlyAdded,
            cstTypesNewlyAdded);
        thrown = new FieldIdOverflowException(index);
        throw new FieldIdOverflowException(index);
      }

      if ((cstMethodRefs.size()) > DexFormat.MAX_MEMBER_IDX + 1) {
        removeItems(cstStringsNewlyAdded, cstFieldRefsNewlyAdded, cstMethodRefsNewlyAdded,
            cstTypesNewlyAdded);
        thrown = new MethodIdOverflowException(index);
        throw new MethodIdOverflowException(index);
      }

      if ((cstTypes.size()) > DexFormat.MAX_TYPE_IDX + 1) {
        removeItems(cstStringsNewlyAdded, cstFieldRefsNewlyAdded, cstMethodRefsNewlyAdded,
            cstTypesNewlyAdded);
        thrown = new TypeIdOverflowException(index);
        throw new TypeIdOverflowException(index);
      }

      synchronized (this) {
        pesimisticFieldsSize += cstFieldRefsNewlyAdded.size();
        pesimisticMethodsSize += cstMethodRefsNewlyAdded.size();
        pesimisticTypesSize += cstTypesNewlyAdded.size();
      }
    }

    cstIndexMaps.add(cstIndexMap);

    return cstIndexMap;
  }

  private interface Door {

    public abstract boolean enter(int index, int fieldsSize, int methodsSize, int typesSize);

    public abstract void inviteNext();

  }

  private class DefaultDoor implements Door {

    private int expectedMainDexTypes;

    @Nonnull
    private final ReentrantLock doorLock = new ReentrantLock();

    @Nonnull
    private final Condition allMarkedTypesProcessed = doorLock.newCondition();

    public DefaultDoor(int numberOfMainDexTypes) {
      this.expectedMainDexTypes = numberOfMainDexTypes;
    }

    @Override
    public boolean enter(int index, int fieldsSize, int methodsSize, int typesSize) {
      boolean fastPath = false;
      if (expectedMainDexTypes > 0 && index != DEFAULT_MULTIDEX_MAIN_DEX_INDEX) {
        doorLock.lock();
        try {
          while (expectedMainDexTypes > 0) {
            allMarkedTypesProcessed.awaitUninterruptibly();
          }
        } finally {
          doorLock.unlock();
        }
      }
      synchronized (ConstantManager.this) {
        if (pesimisticFieldsSize + fieldsSize <= DexFormat.MAX_MEMBER_IDX + 1
            && pesimisticMethodsSize + methodsSize <= DexFormat.MAX_MEMBER_IDX + 1
            && pesimisticTypesSize + typesSize <= DexFormat.MAX_TYPE_IDX + 1) {
          pesimisticFieldsSize += fieldsSize;
          pesimisticMethodsSize += methodsSize;
          pesimisticTypesSize += typesSize;
          fastPath = true;
        } else {
          fastPath = false;
        }
      }
      if (fastPath) {
        //requires only 1 semaphore token, many threads can use the fast path in parallel
        lock.acquireUninterruptibly();
        synchronized (ConstantManager.this) {
          if (pesimisticFieldsSize > DexFormat.MAX_MEMBER_IDX + 1
              || pesimisticMethodsSize > DexFormat.MAX_MEMBER_IDX + 1
              || pesimisticTypesSize > DexFormat.MAX_TYPE_IDX + 1) {
              //things changed while we were acquiring due to someone finishing slowPath
              fastPath = false;
              pesimisticFieldsSize -= fieldsSize;
              pesimisticMethodsSize -= methodsSize;
              pesimisticTypesSize -= typesSize;
              lock.release();
          }
        }
        if (!fastPath) {
          lock.acquireUninterruptibly(SLOW_PATH_PERMITS);
        } else {
          inviteNext();
        }
      } else {
        //requires all semaphore tokens, we want exclusive access
        lock.acquireUninterruptibly(SLOW_PATH_PERMITS);
      }
      return fastPath;
    }

    @Override
    public void inviteNext() {
      doorLock.lock();
      try {
        if (expectedMainDexTypes > 0) {
          //if types marked for main dex are expected and I invite someone that means I am marked
          expectedMainDexTypes--;
        }
        if (expectedMainDexTypes == 0) {
          allMarkedTypesProcessed.signalAll();
        }
      } finally {
        doorLock.unlock();
      }
    }

  }

  private class StrictOrderDoor implements Door {
    @Nonnull
    private final ReentrantLock doorLock = new ReentrantLock();

    @Nonnull
    private final PriorityQueue<QueueElem> queue = new PriorityQueue<QueueElem>(40,
        new Comparator<QueueElem>() {
          @Override
          public int compare(QueueElem o1, QueueElem o2) {
            return o1.priority - o2.priority;
          }
    });

    private int expected;

    public StrictOrderDoor(int firstExpectedIndex) {
      expected = firstExpectedIndex;
    }

    @Override
    public boolean enter(int index, int fieldsSize, int methodsSize, int typesSize) {
      boolean fastPath = false;
      doorLock.lock();
      try {
        QueueElem elem = new QueueElem(index);
        while (index != expected) {
          queue.add(elem);
          elem.getCondition().awaitUninterruptibly();
        }
        synchronized (ConstantManager.this) {
          if (pesimisticFieldsSize + fieldsSize <= DexFormat.MAX_MEMBER_IDX + 1
              && pesimisticMethodsSize + methodsSize <= DexFormat.MAX_MEMBER_IDX + 1
              && pesimisticTypesSize + typesSize <= DexFormat.MAX_TYPE_IDX + 1) {
            //thread safe
            pesimisticFieldsSize += fieldsSize;
            pesimisticMethodsSize += methodsSize;
            pesimisticTypesSize += typesSize;
            //in ordered mode fastPath permit can and has to be aquired here
            //can be aquired because if we are expected then there is no thread on slowPath.
            //has to be aquired to make sure that a thread that wants slowPath won't beat us
            //to aquire its SLOW_PATH_PERMITS and run before us which would break the ordering
            boolean aquired = lock.tryAcquire();
            assert aquired;
            fastPath = true;
          } else {
            //not thread safe because of rollback risk
            fastPath = false;
          }
        }
      } finally {
        doorLock.unlock();
      }
      if (fastPath) {
        inviteNext();
      } else {
        lock.acquireUninterruptibly(SLOW_PATH_PERMITS);
      }
      return fastPath;
    }

    @Override
    public void inviteNext() {
      doorLock.lock();
      try {
        expected++;
        QueueElem elem = queue.poll();
        if (elem != null) {
          elem.getCondition().signal();
        }
      } finally {
        doorLock.unlock();
      }
    }

    private class QueueElem {
      @Nonnull
      private final Condition cond = doorLock.newCondition();
      private final int priority;

      public QueueElem(int priority) {
        super();
        this.priority = priority;
      }

      public Condition getCondition() {
        return cond;
      }
    }
  }

  private void unlockAccordingToPath(boolean fastPath) {
    if (fastPath) {
      lock.release();
    } else {
      lock.release(SLOW_PATH_PERMITS);
    }
  }

  @Nonnull
  public CstIndexMap addDexFile(@Nonnull DexBuffer dexBuffer, int index)
      throws MergingOverflowException {
    CstIndexMap cstIndexMap;
    int fieldsSize = dexBuffer.fieldIds().size();
    int methodsSize = dexBuffer.methodIds().size();
    int typesSize = dexBuffer.typeIds().size();

    assert door != null;
    boolean fastPath = door.enter(index, fieldsSize, methodsSize, typesSize);

    try {
      if (!fastPath) {
        synchronized (this) {
          if (pesimisticFieldsSize + fieldsSize <= DexFormat.MAX_MEMBER_IDX + 1
              && pesimisticMethodsSize + methodsSize <= DexFormat.MAX_MEMBER_IDX + 1
              && pesimisticTypesSize + typesSize <= DexFormat.MAX_TYPE_IDX + 1) {
            //We can change to fastPath afterall
            fastPath = true;
            pesimisticFieldsSize += fieldsSize;
            pesimisticMethodsSize += methodsSize;
            pesimisticTypesSize += typesSize;
          }
        }
        if (fastPath) {
          lock.release(SLOW_PATH_PERMITS - 1);
          assert door != null;
          door.inviteNext();
        }
      }

      if (!bestMergingAccuracy && thrown != null) {
        //if someone else overflowed already, and we are not in bestMergingAccuracy mode
        //let's just assume we overflow as well
        //not a bug, once thrown becomes non null it will stay that way
        throw thrown;
      }
      cstIndexMap = addDexFileInternal(dexBuffer, fastPath, index);
    } finally {
      unlockAccordingToPath(fastPath);
      if (!fastPath) {
        assert door != null;
        door.inviteNext();
      }
    }
    return cstIndexMap;
  }

  private void removeItems(@Nonnull List<String> cstStringsToRemove,
      @Nonnull List<CstFieldRef> cstFieldRefsToRemove,
      @Nonnull List<CstMethodRef> cstMethodRefsToRemove, @Nonnull List<CstType> cstTypesToRemove) {
    string2CstStrings.keySet().removeAll(cstStringsToRemove);
    cstFieldRefs.removeAll(cstFieldRefsToRemove);
    cstMethodRefs.removeAll(cstMethodRefsToRemove);
    cstTypes.removeAll(cstTypesToRemove);
  }

  public boolean validate(@Nonnull DexFile dexFile) {
    return ((dexFile.getStringIds().items().size() == string2CstStrings.size())
        && (dexFile.getFieldIds().items().size() == cstFieldRefs.size())
        && (dexFile.getMethodIds().items().size() == cstMethodRefs.size())
        && (dexFile.getTypeIds().items().size() == cstTypes.size()));
  }

}
