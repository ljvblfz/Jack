/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.jack.dx.io;

import com.android.jack.dx.dex.DexFormat;
import com.android.jack.dx.dex.SizeOf;
import com.android.jack.dx.dex.TableOfContents;
import com.android.jack.dx.io.Code.CatchHandler;
import com.android.jack.dx.io.Code.Try;
import com.android.jack.dx.rop.cst.CstArray;
import com.android.jack.dx.rop.cst.CstCallSiteRef;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstMethodHandleRef.MethodHandleKind;
import com.android.jack.dx.util.ByteInput;
import com.android.jack.dx.util.ByteOutput;
import com.android.jack.dx.util.DexException;
import com.android.jack.dx.util.FileUtils;
import com.android.jack.dx.util.Leb128Utils;
import com.android.jack.dx.util.Mutf8;
import com.android.jack.tools.merger.MergerTools;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * The bytes of a dex file in memory for reading and writing. All int offsets
 * are unsigned.
 */
public final class DexBuffer {
  private byte[] data;
  private final TableOfContents tableOfContents = new TableOfContents();
  private int length = 0;

  private final List<String> strings;

  private final List<Integer> typeIds;

  private final List<String> typeNames;

  private final List<ProtoId> protoIds = new AbstractList<ProtoId>() {
    @Override
    public ProtoId get(int index) {
      checkBounds(index, tableOfContents.protoIds.size);
      return openInternal(tableOfContents.protoIds.off + (SizeOf.PROTO_ID_ITEM * index))
          .readProtoId();
    }

    @Override
    public int size() {
      return tableOfContents.protoIds.size;
    }
  };

  @Nonnull
  private final List<FieldId> fieldIds;
  @Nonnull
  private final List<MethodId> methodIds;
  @Nonnull
  private final List<MethodHandleId> methodHandleIds;
  @Nonnull
  private final List<Integer> callSiteIds;
  @Nonnull
  private final Section internalSection;

  /**
   * Creates a new dex buffer defining no classes.
   */
  public DexBuffer() {
    this.data = new byte[0];
    this.internalSection = new Section(0);
    this.strings = Collections.emptyList();
    this.typeIds = Collections.emptyList();
    this.typeNames = Collections.emptyList();
    this.fieldIds = Collections.emptyList();
    this.methodIds = Collections.emptyList();
    this.methodHandleIds = Collections.emptyList();
    this.callSiteIds = Collections.emptyList();
  }

  /**
   * Creates a new dex buffer that reads from {@code data}. It is an error to
   * modify {@code data} after using it to create a dex buffer.
   */
  public DexBuffer(byte[] data) {
    this.data = data;
    this.internalSection = new Section(0);
    this.length = data.length;
    this.tableOfContents.readFrom(this);
    this.strings = readStrings();
    this.typeIds = readTypeIds();
    this.typeNames = readTypeNames(this.strings, this.typeIds);
    this.fieldIds = readFieldIds();
    this.methodIds = readMethodIds();
    this.methodHandleIds = readMethodHandleIds();
    this.callSiteIds = readCallSiteIds();
  }

  /**
   * Creates a new dex buffer of the dex in {@code in}.
   */
  public DexBuffer(@Nonnull InputStream in, @Nonnull Location location) throws CannotReadException {
    loadFrom(in, location);
    this.internalSection = new Section(0);
    this.strings = readStrings();
    this.typeIds = readTypeIds();
    this.typeNames = readTypeNames(this.strings, this.typeIds);
    this.fieldIds = readFieldIds();
    this.methodIds = readMethodIds();
    this.methodHandleIds = readMethodHandleIds();
    this.callSiteIds = readCallSiteIds();
  }

  /**
   * Creates a new dex buffer from the dex file {@code file}.
   */
  public DexBuffer(File file) throws CannotReadException, CannotCloseException {
    FileLocation fileLocation = new FileLocation(file);
    if (FileUtils.hasArchiveSuffix(file.getName())) {
      try (ZipFile zipFile = new ZipFile(file)) {
        ZipEntry entry = zipFile.getEntry(DexFormat.DEX_IN_JAR_NAME);
        if (entry != null) {
          try (InputStream is = zipFile.getInputStream(entry)) {
            loadFrom(is, new ZipLocation(fileLocation, entry));
          } catch (IOException e) {
            throw new CannotCloseException(new ZipLocation(fileLocation, entry), e);
          }
        } else {
          throw new DexException("Expected " + DexFormat.DEX_IN_JAR_NAME + " in " + file);
        }
      } catch (IOException e) {
        throw new CannotCloseException(fileLocation, e);
      }
    } else if (file.getName().endsWith(".dex")) {
      try (InputStream is = new FileInputStream(file)) {
        loadFrom(is, fileLocation);
      } catch (IOException e) {
        throw new CannotCloseException(fileLocation, e);
      }
    } else {
      throw new DexException("unknown output extension: " + file);
    }
    this.internalSection = new Section(0);
    this.strings = readStrings();
    this.typeIds = readTypeIds();
    this.typeNames = readTypeNames(this.strings, this.typeIds);
    this.fieldIds = readFieldIds();
    this.methodIds = readMethodIds();
    this.methodHandleIds = readMethodHandleIds();
    this.callSiteIds = readCallSiteIds();
  }

  @Nonnull
  private List<String> readStrings() {
    Section strings = openInternal(tableOfContents.stringIds.off);
    String[] result = new String[tableOfContents.stringIds.size];
    for (int i = 0; i < tableOfContents.stringIds.size; ++i) {
      result[i] = strings.readString();
    }
    return Arrays.asList(result);
  }

  @Nonnull
  private List<Integer> readCallSiteIds() {
    if (tableOfContents.dexVersion != DexFormat.O_BETA2_DEX_VERSION) {
      return Collections.emptyList();
    }

    Section callSiteIds = openInternal(tableOfContents.callSiteIds.off);
    Integer[] result = new Integer[tableOfContents.callSiteIds.size];
    for (int i = 0; i < tableOfContents.callSiteIds.size; ++i) {
      result[i] = Integer.valueOf(callSiteIds.readInt());
    }
    return Arrays.asList(result);
  }

  @Nonnull
  private List<MethodHandleId> readMethodHandleIds() {
    if (tableOfContents.dexVersion != DexFormat.O_BETA2_DEX_VERSION) {
      return Collections.emptyList();
    }

    Section methodHandleIds = openInternal(tableOfContents.methodHandleIds.off);
    MethodHandleId[] result = new MethodHandleId[tableOfContents.methodHandleIds.size];
    for (int i = 0; i < tableOfContents.methodHandleIds.size; ++i) {
      short kindValue = methodHandleIds.readShort();
      methodHandleIds.readShort(); // reserved
      short memberIdx = methodHandleIds.readShort();
      methodHandleIds.readShort(); // reserved
      result[i] = new MethodHandleId(this, MethodHandleKind.getKind(kindValue), memberIdx);
    }

    return Arrays.asList(result);
  }

  @Nonnull
  private List<Integer> readTypeIds() {
    Section typeIds = openInternal(tableOfContents.typeIds.off);
    Integer[] result = new Integer[tableOfContents.typeIds.size];
    for (int i = 0; i < tableOfContents.typeIds.size; ++i) {
      result[i] = Integer.valueOf(typeIds.readInt());
    }
    return Arrays.asList(result);
  }

  @Nonnull
  private List<String> readTypeNames(List<String> strings, List<Integer> typeIds) {
    String[] result = new String[tableOfContents.typeIds.size];
    for (int i = 0; i < tableOfContents.typeIds.size; ++i) {
      result[i] = strings.get(typeIds.get(i).intValue());
    }
    return Arrays.asList(result);
  }

  @Nonnull
  private List<FieldId> readFieldIds() {
    Section fieldIds = openInternal(tableOfContents.fieldIds.off);
    FieldId[] result = new FieldId[tableOfContents.fieldIds.size];
    for (int i = 0; i < tableOfContents.fieldIds.size; ++i) {
      result[i] = fieldIds.readFieldId();
    }
    return Arrays.asList(result);
  }

  @Nonnull
  private List<MethodId> readMethodIds() {
    Section methodIds = openInternal(tableOfContents.methodIds.off);
    MethodId[] result = new MethodId[tableOfContents.methodIds.size];
    for (int i = 0; i < tableOfContents.methodIds.size; ++i) {
      result[i] = methodIds.readMethodId();
    }
    return Arrays.asList(result);
  }

  private void loadFrom(@Nonnull InputStream in, @Nonnull Location location)
      throws CannotReadException {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];

    try {
      int count;
      while ((count = in.read(buffer)) != -1) {
        bytesOut.write(buffer, 0, count);
      }
    } catch (IOException e) {
      throw new CannotReadException(location, e);
    }

    this.data = bytesOut.toByteArray();
    this.length = data.length;
    this.tableOfContents.readFrom(this);
  }

  private static void checkBounds(int index, int length) {
    if (index < 0 || index >= length) {
      throw new IndexOutOfBoundsException("index:" + index + ", length=" + length);
    }
  }

  public void writeTo(OutputStream out) throws IOException {
    out.write(data);
  }

  public void writeTo(File dexOut) throws IOException {
    OutputStream out = new FileOutputStream(dexOut);
    writeTo(out);
    out.close();
  }

  public TableOfContents getTableOfContents() {
    return tableOfContents;
  }

  @Nonnull
  private Section openInternal(@Nonnegative int position) {
    internalSection.initialPosition = internalSection.position = position;
    return internalSection;
  }

  public Section open(int position) {
    if (position < 0 || position > length) {
      throw new IllegalArgumentException("position=" + position + " length=" + length);
    }
    return new Section(position);
  }

  public Section appendSection(int maxByteCount, String name) {
    int limit = fourByteAlign(length + maxByteCount);
    Section result = new Section(name, length, limit);
    length = limit;
    return result;
  }

  public void noMoreSections() {
    data = new byte[length];
  }

  public int getLength() {
    return length;
  }

  public static int fourByteAlign(int position) {
    return (position + 3) & ~3;
  }

  public byte[] getBytes() {
    return data;
  }

  @Nonnull
  public List<String> strings() {
    return strings;
  }

  @Nonnull
  public List<Integer> typeIds() {
    return typeIds;
  }

  @Nonnull
  public List<String> typeNames() {
    return typeNames;
  }

  @Nonnull
  public List<ProtoId> protoIds() {
    return protoIds;
  }

  @Nonnull
  public List<FieldId> fieldIds() {
    return fieldIds;
  }

  @Nonnull
  public List<MethodId> methodIds() {
    return methodIds;
  }

  @Nonnull
  public List<MethodHandleId> methodHandleIds() {
    return methodHandleIds;
  }

  @Nonnull
  public List<Integer> callSiteIds() {
    return callSiteIds;
  }

  public Iterable<ClassDef> classDefs() {
    return new Iterable<ClassDef>() {
      @Override
      public Iterator<ClassDef> iterator() {
        if (!tableOfContents.classDefs.exists()) {
          return Collections.<ClassDef>emptySet().iterator();
        }
        return new Iterator<ClassDef>() {
          private final DexBuffer.Section in = open(tableOfContents.classDefs.off);
          private int count = 0;

          @Override
          public boolean hasNext() {
            return count < tableOfContents.classDefs.size;
          }

          @Override
          public ClassDef next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            count++;
            return in.readClassDef();
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public TypeList readTypeList(int offset) {
    if (offset == 0) {
      return TypeList.EMPTY;
    }
    return openInternal(offset).readTypeList();
  }

  public ClassData readClassData(ClassDef classDef) {
    int offset = classDef.getClassDataOffset();
    if (offset == 0) {
      throw new IllegalArgumentException("offset == 0");
    }
    return openInternal(offset).readClassData();
  }

  public Code readCode(ClassData.Method method) {
    int offset = method.getCodeOffset();
    if (offset == 0) {
      throw new IllegalArgumentException("offset == 0");
    }
    return openInternal(offset).readCode();
  }

  @Nonnull
  public CstCallSiteRef readCstCallSiteRef(@Nonnull CstIndexMap cstIndexMap,
      @Nonnegative int cstCallsiteIdx) {
    CstCallSiteRefBuilder cstCallSiteBuilder =
        new CstCallSiteRefBuilder(cstIndexMap, cstCallsiteIdx);
    return cstCallSiteBuilder.build();
  }

  private class CstCallSiteRefBuilder {

    class CallSiteEncodedArrayReader extends EncodedValueReader {

      private CstArray.List callSiteArrayList;

      private int idx = 0;

      public CallSiteEncodedArrayReader(@Nonnull ByteInput in) {
        super(DexBuffer.this, in);
      }

      public CstArray getCstArray() {
        callSiteArrayList.setImmutable();
        return new CstArray(callSiteArrayList);
      }

      @Override
      protected void visitArray(int size) {
        callSiteArrayList = new CstArray.List(size);
      }

      @Override
      protected void visitString(int index) {
        callSiteArrayList.set(idx++, cstIndexMap.getCstString(index));
      }

      @Override
      protected void visitMethodType(int prototypeIdx) {
        callSiteArrayList.set(idx++, cstIndexMap.getCstPrototype(prototypeIdx));
      }

      @Override
      protected void visitMethodHandle(int methodHandleIdx) {
        callSiteArrayList.set(idx++, cstIndexMap.getCstMethodHandle(methodHandleIdx));
      }

      @Override
      protected void visitPrimitive(int type, int arg, int size) {
        callSiteArrayList.set(idx++, MergerTools.createConstant(in, type, arg));
      }

      @Override
      protected void visitEncodedNull(int argAndType) {
        throw new AssertionError("Unsupported encoded value.");
      }

      @Override
      protected void visitEncodedBoolean(int argAndType) {
        throw new AssertionError("Unsupported encoded value.");
      }

      @Override
      protected void visitAnnotation(int typeIndex, int size) {
        throw new AssertionError("Unsupported encoded value.");
      }

      @Override
      protected void visitAnnotationName(int nameIndex) {
        throw new AssertionError("Unsupported encoded value.");
      }

      @Override
      protected void visitAnnotationValue(int argAndType) {
        throw new AssertionError("Unsupported encoded value.");
      }

      @Override
      protected void visitArrayValue(int argAndType) {
        throw new AssertionError("Unsupported encoded value.");
      }

      @Override
      protected void visitField(int type, int index) {
        throw new AssertionError("Unsupported encoded value.");
      }

      @Override
      protected void visitMethod(int index) {
        throw new AssertionError("Unsupported encoded value.");
      }

      @Override
      protected void visitType(int index) {
        callSiteArrayList.set(idx++, cstIndexMap.getType(index));
      }
    }

    @Nonnull
    private final CallSiteEncodedArrayReader callSiteReader;

    @Nonnull
    private final CstIndexMap cstIndexMap;

    public CstCallSiteRefBuilder(@Nonnull CstIndexMap cstIndexMap,
        @Nonnegative int encodedArrayOffset) {
      callSiteReader = new CallSiteEncodedArrayReader(new Section(encodedArrayOffset));
      this.cstIndexMap = cstIndexMap;
    }

    @Nonnull
    public CstCallSiteRef build() {
      callSiteReader.readArray();
      return new CstCallSiteRef(callSiteReader.getCstArray());
    }
  }

  /**
   * TODO(jack team)
   */
  public final class Section implements ByteInput, ByteOutput {
    private final String name;
    private int position;
    private final int limit;
    private int initialPosition;

    private Section(String name, int position, int limit) {
      this.name = name;
      this.position = this.initialPosition = position;
      this.limit = limit;
    }

    private Section(int position) {
      this("section", position, data.length);
    }

    public int getPosition() {
      return position;
    }

    public int readInt() {
      int result = (data[position] & 0xff) | (data[position + 1] & 0xff) << 8
          | (data[position + 2] & 0xff) << 16 | (data[position + 3] & 0xff) << 24;
      position += 4;
      return result;
    }

    public short readShort() {
      int result = (data[position] & 0xff) | (data[position + 1] & 0xff) << 8;
      position += 2;
      return (short) result;
    }

    public int readUnsignedShort() {
      return readShort() & 0xffff;
    }

    @Override
    public byte readByte() {
      return (byte) (data[position++] & 0xff);
    }

    public byte[] readByteArray(int length) {
      byte[] result = Arrays.copyOfRange(data, position, position + length);
      position += length;
      return result;
    }

    public short[] readShortArray(int length) {
      short[] result = new short[length];
      for (int i = 0; i < length; i++) {
        result[i] = readShort();
      }
      return result;
    }

    public int readUleb128() {
      return Leb128Utils.readUnsignedLeb128(this);
    }

    public int readUleb128p1() {
      return Leb128Utils.readUnsignedLeb128(this) - 1;
    }

    public int readSleb128() {
      return Leb128Utils.readSignedLeb128(this);
    }

    public TypeList readTypeList() {
      assertFourByteAligned();
      int size = readInt();
      short[] types = new short[size];
      for (int i = 0; i < size; i++) {
        types[i] = readShort();
      }
      position = DexBuffer.fourByteAlign(position);
      return new TypeList(DexBuffer.this, types);
    }

    public String readString() {
      int offset = readInt();
      int savedPosition = position;
      position = offset;
      try {
        int expectedLength = readUleb128();
        String result = Mutf8.decode(this, new char[expectedLength]);
        if (result.length() != expectedLength) {
          throw new DexException("Declared length " + expectedLength
              + " doesn't match decoded length of " + result.length());
        }
        return result;
      } catch (UTFDataFormatException e) {
        throw new DexException(e);
      } finally {
        position = savedPosition;
      }
    }

    public FieldId readFieldId() {
      int declaringClassIndex = readUnsignedShort();
      int typeIndex = readUnsignedShort();
      int nameIndex = readInt();
      return new FieldId(DexBuffer.this, declaringClassIndex, typeIndex, nameIndex);
    }

    public MethodId readMethodId() {
      int declaringClassIndex = readUnsignedShort();
      int protoIndex = readUnsignedShort();
      int nameIndex = readInt();
      return new MethodId(DexBuffer.this, declaringClassIndex, protoIndex, nameIndex);
    }

    public ProtoId readProtoId() {
      int shortyIndex = readInt();
      int returnTypeIndex = readInt();
      int parametersOffset = readInt();
      return new ProtoId(DexBuffer.this, shortyIndex, returnTypeIndex, parametersOffset);
    }

    public ClassDef readClassDef() {
      int offset = getPosition();
      int type = readInt();
      int accessFlags = readInt();
      int supertype = readInt();
      int interfacesOffset = readInt();
      int sourceFileIndex = readInt();
      int annotationsOffset = readInt();
      int classDataOffset = readInt();
      int staticValuesOffset = readInt();
      return new ClassDef(DexBuffer.this,
          offset,
          type,
          accessFlags,
          supertype,
          interfacesOffset,
          sourceFileIndex,
          annotationsOffset,
          classDataOffset,
          staticValuesOffset);
    }

    private Code readCode() {
      int registersSize = readUnsignedShort();
      int insSize = readUnsignedShort();
      int outsSize = readUnsignedShort();
      int triesSize = readUnsignedShort();
      int debugInfoOffset = readInt();
      int instructionsSize = readInt();
      short[] instructions = readShortArray(instructionsSize);
      Try[] tries;
      CatchHandler[] catchHandlers;
      if (triesSize > 0) {
        if (instructions.length % 2 == 1) {
          readShort(); // padding
        }

        /*
         * We can't read the tries until we've read the catch handlers.
         * Unfortunately they're in the opposite order in the dex file
         * so we need to read them out-of-order.
         */
        int savedPosition = position;
        skip(triesSize * SizeOf.TRY_ITEM);
        catchHandlers = readCatchHandlers();
        position = savedPosition;
        tries = readTries(triesSize, catchHandlers);
      } else {
        tries = new Try[0];
        catchHandlers = new CatchHandler[0];
      }
      return new Code(registersSize,
          insSize,
          outsSize,
          debugInfoOffset,
          instructions,
          tries,
          catchHandlers);
    }

    private CatchHandler[] readCatchHandlers() {
      int baseOffset = position;
      int catchHandlersSize = readUleb128();
      CatchHandler[] result = new CatchHandler[catchHandlersSize];
      for (int i = 0; i < catchHandlersSize; i++) {
        int offset = position - baseOffset;
        result[i] = readCatchHandler(offset);
      }
      return result;
    }

    private Try[] readTries(int triesSize, CatchHandler[] catchHandlers) {
      Try[] result = new Try[triesSize];
      for (int i = 0; i < triesSize; i++) {
        int startAddress = readInt();
        int instructionCount = readUnsignedShort();
        int handlerOffset = readUnsignedShort();
        int catchHandlerIndex = findCatchHandlerIndex(catchHandlers, handlerOffset);
        result[i] = new Try(startAddress, instructionCount, catchHandlerIndex);
      }
      return result;
    }

    private int findCatchHandlerIndex(CatchHandler[] catchHandlers, int offset) {
      for (int i = 0; i < catchHandlers.length; i++) {
        CatchHandler catchHandler = catchHandlers[i];
        if (catchHandler.getOffset() == offset) {
          return i;
        }
      }
      throw new IllegalArgumentException();
    }

    private CatchHandler readCatchHandler(int offset) {
      int size = readSleb128();
      int handlersCount = Math.abs(size);
      int[] typeIndexes = new int[handlersCount];
      int[] addresses = new int[handlersCount];
      for (int i = 0; i < handlersCount; i++) {
        typeIndexes[i] = readUleb128();
        addresses[i] = readUleb128();
      }
      int catchAllAddress = size <= 0 ? readUleb128() : -1;
      return new CatchHandler(typeIndexes, addresses, catchAllAddress, offset);
    }

    private ClassData readClassData() {
      int staticFieldsSize = readUleb128();
      int instanceFieldsSize = readUleb128();
      int directMethodsSize = readUleb128();
      int virtualMethodsSize = readUleb128();
      ClassData.Field[] staticFields = readFields(staticFieldsSize);
      ClassData.Field[] instanceFields = readFields(instanceFieldsSize);
      ClassData.Method[] directMethods = readMethods(directMethodsSize);
      ClassData.Method[] virtualMethods = readMethods(virtualMethodsSize);
      return new ClassData(staticFields, instanceFields, directMethods, virtualMethods);
    }

    private ClassData.Field[] readFields(int count) {
      ClassData.Field[] result = new ClassData.Field[count];
      int fieldIndex = 0;
      for (int i = 0; i < count; i++) {
        fieldIndex += readUleb128(); // field index diff
        int accessFlags = readUleb128();
        result[i] = new ClassData.Field(fieldIndex, accessFlags);
      }
      return result;
    }

    private ClassData.Method[] readMethods(int count) {
      ClassData.Method[] result = new ClassData.Method[count];
      int methodIndex = 0;
      for (int i = 0; i < count; i++) {
        methodIndex += readUleb128(); // method index diff
        int accessFlags = readUleb128();
        int codeOff = readUleb128();
        result[i] = new ClassData.Method(methodIndex, accessFlags, codeOff);
      }
      return result;
    }

    public Annotation readAnnotation() {
      byte visibility = readByte();
      int typeIndex = readUleb128();
      int size = readUleb128();
      int[] names = new int[size];
      EncodedValue[] values = new EncodedValue[size];
      for (int i = 0; i < size; i++) {
        names[i] = readUleb128();
        values[i] = readEncodedValue();
      }
      return new Annotation(DexBuffer.this, visibility, typeIndex, names, values);
    }

    public EncodedValue readEncodedValue() {
      int start = position;
      new EncodedValueReader(DexBuffer.this, this).readValue();
      int end = position;
      return new EncodedValue(Arrays.copyOfRange(data, start, end));
    }

    public EncodedValue readEncodedArray() {
      int start = position;
      new EncodedValueReader(DexBuffer.this, this).readArray();
      int end = position;
      return new EncodedValue(Arrays.copyOfRange(data, start, end));
    }

    private void ensureCapacity(int size) {
      if (position + size > limit) {
        throw new DexException("Section limit " + limit + " exceeded by " + name);
      }
    }

    public void skip(int count) {
      if (count < 0) {
        throw new IllegalArgumentException();
      }
      ensureCapacity(count);
      position += count;
    }

    /**
     * Writes 0x00 until the position is aligned to a multiple of 4.
     */
    public void alignToFourBytes() {
      int unalignedCount = position;
      position = DexBuffer.fourByteAlign(position);
      for (int i = unalignedCount; i < position; i++) {
        data[i] = 0;
      }
    }

    public void assertFourByteAligned() {
      if ((position & 3) != 0) {
        throw new IllegalStateException("Not four byte aligned!");
      }
    }

    public void write(byte[] bytes) {
      ensureCapacity(bytes.length);
      System.arraycopy(bytes, 0, data, position, bytes.length);
      position += bytes.length;
    }

    @Override
    public void writeByte(int b) {
      ensureCapacity(1);
      data[position++] = (byte) b;
    }

    public void writeShort(short i) {
      ensureCapacity(2);
      data[position] = (byte) i;
      data[position + 1] = (byte) (i >>> 8);
      position += 2;
    }

    public void writeUnsignedShort(int i) {
      short s = (short) i;
      if (i != (s & 0xffff)) {
        throw new IllegalArgumentException("Expected an unsigned short: " + i);
      }
      writeShort(s);
    }

    public void write(short[] shorts) {
      for (short s : shorts) {
        writeShort(s);
      }
    }

    public void writeInt(int i) {
      ensureCapacity(4);
      data[position] = (byte) i;
      data[position + 1] = (byte) (i >>> 8);
      data[position + 2] = (byte) (i >>> 16);
      data[position + 3] = (byte) (i >>> 24);
      position += 4;
    }

    public void writeUleb128(int i) {
      try {
        Leb128Utils.writeUnsignedLeb128(this, i);
        ensureCapacity(0);
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new DexException("Section limit " + limit + " exceeded by " + name);
      }
    }

    public void writeUleb128p1(int i) {
      writeUleb128(i + 1);
    }

    public void writeSleb128(int i) {
      try {
        Leb128Utils.writeSignedLeb128(this, i);
        ensureCapacity(0);
      } catch (ArrayIndexOutOfBoundsException e) {
        throw new DexException("Section limit " + limit + " exceeded by " + name);
      }
    }

    public void writeStringData(String value) {
      try {
        int length = value.length();
        writeUleb128(length);
        write(Mutf8.encode(value));
        writeByte(0);
      } catch (UTFDataFormatException e) {
        throw new AssertionError();
      }
    }

    public void writeTypeList(TypeList typeList) {
      short[] types = typeList.getTypes();
      writeInt(types.length);
      for (short type : types) {
        writeShort(type);
      }
      alignToFourBytes();
    }

    /**
     * Returns the number of bytes remaining in this section.
     */
    public int remaining() {
      return limit - position;
    }

    /**
     * Returns the number of bytes used by this section.
     */
    public int used() {
      return position - initialPosition;
    }
  }
}
