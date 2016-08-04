/*
* Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.jayce.v0003.io;

import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JMethodCall.DispatchKind;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.jayce.DeclaredTypeNode;
import com.android.jack.jayce.JayceFormatException;
import com.android.jack.jayce.JayceInternalReader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0003.NNode;
import com.android.jack.jayce.v0003.Version;
import com.android.jack.jayce.v0003.nodes.HasCatchBlockIds;
import com.android.jack.jayce.v0003.nodes.HasSourceInfo;
import com.android.jack.jayce.v0003.nodes.NDeclaredType;
import com.android.jack.jayce.v0003.nodes.NMethod;
import com.android.jack.jayce.v0003.nodes.NMethodCall.ReceiverKind;
import com.android.jack.jayce.v0003.nodes.NSourceInfo;
import com.android.jack.jayce.v0003.util.DispatchKindIdHelper;
import com.android.jack.jayce.v0003.util.FieldRefKindIdHelper;
import com.android.jack.jayce.v0003.util.MethodKindIdHelper;
import com.android.jack.jayce.v0003.util.ReceiverKindIdHelper;
import com.android.jack.jayce.v0003.util.RetentionPolicyIdHelper;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Percent;
import com.android.sched.util.log.stats.PercentImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jayce internal reader implementation.
 */
public class JayceInternalReaderImpl implements JayceInternalReader {
  @Nonnull
  public static final StatisticId<Percent> SKIPPED_TYPE_STRUCTURE = new StatisticId<Percent>(
      "jack.jayce-to-nnode.structure.skipped", "Type structure reading skipped by the reader",
      PercentImpl.class, Percent.class);
  @Nonnull
  public static final StatisticId<Percent> SKIPPED_BODY = new StatisticId<Percent>(
      "jack.jayce-to-nnode.body.skipped", "Method body reading skipped by the reader",
      PercentImpl.class, Percent.class);

  @Nonnull
  private final Tokenizer tokenizer;

  @Nonnull
  private NodeLevel nodeLevel = NodeLevel.FULL;

  @CheckForNull
  private NDeclaredType type;

  @CheckForNull
  private String currentFileName;

  @Nonnegative
  private int currentLine;

  @Nonnull
  private final List<String> currentCatchBlockList = new ArrayList<String>();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  public JayceInternalReaderImpl(@Nonnull InputStream in) {
    this.tokenizer = new Tokenizer(in);
  }

  @Nonnull
  public NodeLevel getNodeLevel() {
    return nodeLevel;
  }

  @CheckForNull
  public String readId() throws IOException {
    return readString();
  }

  @CheckForNull
  public String readCurrentFileName() throws IOException {
    if (tokenizer.readOpenFileName()) {
      currentFileName = readString();
      tokenizer.readCloseFileName();
    }
    return currentFileName;
  }

  @Nonnegative
  public int readCurrentLine() throws IOException {
    if (tokenizer.readOpenLineInfo()) {
      currentLine = readInt();
      tokenizer.readCloseLineInfo();
    }
    return currentLine;
  }

  @Nonnull
  public JRetentionPolicy readRetentionPolicyEnum() throws IOException {
    return RetentionPolicyIdHelper.getValue(readByte());
  }

  @Nonnull
  public FieldKind readFieldRefKindEnum() throws IOException {
    return FieldRefKindIdHelper.getValue(readByte());
  }

  @Nonnull
  public MethodKind readMethodKindEnum() throws IOException {
    return MethodKindIdHelper.getValue(readByte());
  }

  @Nonnull
  public ReceiverKind readReceiverKindEnum() throws IOException {
    return ReceiverKindIdHelper.getValue(readByte());
  }

  @Nonnull
  public DispatchKind readDispatchKindEnum() throws IOException {
    return DispatchKindIdHelper.getValue(readByte());
  }

  @CheckForNull
  public String readString() throws IOException {
    return tokenizer.readString();
  }

  @CheckForNull
  public byte[] readBuffer() throws IOException {
    return tokenizer.readBuffer();
  }

  @Nonnull
  public List<String> readIds() throws IOException {
    tokenizer.readOpen();
    int length = readInt();
    List<String> ids = new ArrayList<String>(length);
    for (int i = 0; i < length; i++) {
      ids.add(readId());
    }
    tokenizer.readClose();
    return ids;
  }

  public void readCatchBlockIds() throws IOException {
    if (tokenizer.readOpenCatchBlockIdAdd()) {
      int length = tokenizer.readInt();
      for (int i = 0; i < length; i++) {
        String id = readId();
        currentCatchBlockList.add(id);
        assert currentCatchBlockList.indexOf(id) == currentCatchBlockList.lastIndexOf(id);
      }
      tokenizer.readCloseCatchBlockId();
    }
    if (tokenizer.readOpenCatchBlockIdRemove()) {
      int length = tokenizer.readInt();
      for (int i = 0; i < length; i++) {
        String id = readId();
        currentCatchBlockList.remove(id);
        assert !currentCatchBlockList.contains(id);
      }
      tokenizer.readCloseCatchBlockId();
    }
  }

  @SuppressWarnings("unchecked")
  @CheckForNull
  public <T extends NNode> T readNode(@Nonnull Class<T> nodeClass) throws IOException,
      JayceFormatException {
    String fileName = readCurrentFileName();
    int startLine = readCurrentLine();

    readCatchBlockIds();

    Token token = tokenizer.next();

    if (token == Token.NULL) {
      return null;
    }



    tokenizer.readOpen();
    NNode node;
    try {
      node = token.newNode();
    } catch (InvalidTokenException e) {
      throw new ParseException(
          "Unexpected token " + token.toString() + " while expecting node.", e);
    }
    Percent statistic = null;
    if (token == Token.METHOD_BODY) {
      statistic = tracer.getStatistic(SKIPPED_BODY);
    } else if (node instanceof NDeclaredType) {
      tracer.getStatistic(SKIPPED_TYPE_STRUCTURE).add(nodeLevel == NodeLevel.TYPES);
    }

    if (!nodeClass.isAssignableFrom(node.getClass())) {
      throw new JayceFormatException("Unexpected node " + node.getClass().getSimpleName() + ", "
          + nodeClass.getSimpleName() + " was expected.");
    }

    if (nodeLevel != NodeLevel.TYPES && node instanceof HasSourceInfo) {
      NSourceInfo sourceInfo = new NSourceInfo();
      sourceInfo.fileName = fileName;
      sourceInfo.startLine = startLine;
      ((HasSourceInfo) node).setSourceInfos(sourceInfo);
    }
    if (node instanceof HasCatchBlockIds) {
      ((HasCatchBlockIds) node).setCatchBlockIds(new ArrayList<String>(currentCatchBlockList));
    }
    /* readContent can stop in the middle of the node data when nodeLevel is NodeLevel.TYPES
     * meaning we can't read anything after and we have to skip source info.
     */
    node.readContent(this);
    if (nodeLevel != NodeLevel.TYPES) {
      readSourceInfoEnd(node);
      assert !(node instanceof NMethod) || currentCatchBlockList.isEmpty();
      tokenizer.readClose();
    }

    if (nodeLevel.keep(token.getNodeLevel())) {
      if (statistic != null) {
        statistic.addFalse();
      }
      return (T) node;
    } else {
      if (statistic != null) {
        statistic.addTrue();
      }
      return null;
    }
  }

  private void readSourceInfoEnd(@Nonnull NNode node)
      throws IOException {
    if (node instanceof HasSourceInfo) {
      NSourceInfo sourceInfo = ((HasSourceInfo) node).getSourceInfos();
      sourceInfo.endLine = readCurrentLine();
      if (sourceInfo.startLine == 0
              && sourceInfo.endLine == 0
              && !(node instanceof NDeclaredType)) {
        ((HasSourceInfo) node).setSourceInfos(NSourceInfo.UNKNOWN);
      }
    }
  }

  @Nonnull
  public <T extends NNode> List<T> readNodes(@Nonnull Class<T> nodeClass) throws IOException,
      JayceFormatException {
    tokenizer.readOpen();
    int length = readInt();
    List<T> nodes = new ArrayList<T>(length);
    for (int i = 0; i < length; i++) {
      T node = readNode(nodeClass);
      if (node != null) {
        nodes.add(node);
        node.setIndex(i);
      }
    }
    tokenizer.readClose();
    return nodes;

  }

  public int readInt() throws IOException {
    return tokenizer.readInt();
  }

  public byte readByte() throws IOException {
    return tokenizer.readByte();
  }

  public boolean readBoolean() throws IOException {
    return tokenizer.readBoolean();
  }

  public long readLong() throws IOException {
    return tokenizer.readLong();
  }

  public short readShort() throws IOException {
    return tokenizer.readShort();
  }

  public char readChar() throws IOException {
    return tokenizer.readChar();
  }

  public float readFloat() throws IOException {
    return tokenizer.readFloat();
  }

  public double readDouble() throws IOException {
    return tokenizer.readDouble();
  }

  @Override
  @Nonnull
  public DeclaredTypeNode readType(@Nonnull NodeLevel nodeLevel) throws IOException,
      JayceFormatException {
    if (type == null) {
      this.nodeLevel = nodeLevel;
      type = readNode(NDeclaredType.class);
    }
    assert type != null;
    return type;
  }

  @Override
  public int getCurrentMinor() {
    return Version.CURRENT_MINOR;
  }

  @Override
  public int getMinorMin() {
    return Version.MINOR_MIN;
  }
}
