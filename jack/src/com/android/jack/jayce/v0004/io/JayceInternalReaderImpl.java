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

package com.android.jack.jayce.v0004.io;

import com.android.jack.Jack;
import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JMethodCall.DispatchKind;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.jayce.DeclaredTypeNode;
import com.android.jack.jayce.JayceFormatException;
import com.android.jack.jayce.JayceInternalReader;
import com.android.jack.jayce.NodeLevel;
import com.android.jack.jayce.v0004.NNode;
import com.android.jack.jayce.v0004.Version;
import com.android.jack.jayce.v0004.nodes.HasCatchBlockIds;
import com.android.jack.jayce.v0004.nodes.HasSourceInfo;
import com.android.jack.jayce.v0004.nodes.NDeclaredType;
import com.android.jack.jayce.v0004.nodes.NMethod;
import com.android.jack.jayce.v0004.nodes.NMethodCall.ReceiverKind;
import com.android.jack.jayce.v0004.util.DispatchKindIdHelper;
import com.android.jack.jayce.v0004.util.FieldRefKindIdHelper;
import com.android.jack.jayce.v0004.util.MethodKindIdHelper;
import com.android.jack.jayce.v0004.util.ReceiverKindIdHelper;
import com.android.jack.jayce.v0004.util.RetentionPolicyIdHelper;
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

  @Nonnull
  private final SourceInfoFactory sif = Jack.getSession().getSourceInfoFactory();

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

  public void skipId() throws IOException {
    skipString();
  }

  @CheckForNull
  public String readCurrentFileName() throws IOException {
    if (tokenizer.readOpenFileName()) {
      currentFileName = readString();
      // UNKNOW_LINE_NUMBER is not dump for unknown debug information, reset it automatically.
      // Current file name sets to null means unknown debug information.
      if (currentFileName == null) {
        currentLine = SourceInfo.UNKNOWN_LINE_NUMBER;
      }
      tokenizer.readCloseFileName();
    }
    return currentFileName;
  }

  public void skipCurrentFileName() throws IOException {
    if (tokenizer.readOpenFileName()) {
      skipString();
      currentLine = SourceInfo.UNKNOWN_LINE_NUMBER;
      tokenizer.readCloseFileName();
    }
  }

  @Nonnegative
  public int readCurrentLine() throws IOException {
    if (tokenizer.readOpenLineInfo()) {
      currentLine = readInt();
      tokenizer.readCloseLineInfo();
    }
    return currentLine;
  }

  public void skipCurrentLine() throws IOException {
    if (tokenizer.readOpenLineInfo()) {
      skipInt();
      tokenizer.readCloseLineInfo();
    }
  }

  @Nonnull
  public JRetentionPolicy readRetentionPolicyEnum() throws IOException {
    return RetentionPolicyIdHelper.getValue(readByte());
  }

  public void skipRetentionPolicyEnum() throws IOException {
    skipByte();
  }

  @Nonnull
  public FieldKind readFieldRefKindEnum() throws IOException {
    return FieldRefKindIdHelper.getValue(readByte());
  }

  public void skipFieldRefKindEnum() throws IOException {
    skipByte();
  }

  @Nonnull
  public MethodKind readMethodKindEnum() throws IOException {
    return MethodKindIdHelper.getValue(readByte());
  }

  public void skipMethodKindEnum() throws IOException {
    skipByte();
  }

  @Nonnull
  public ReceiverKind readReceiverKindEnum() throws IOException {
    return ReceiverKindIdHelper.getValue(readByte());
  }

  public void skipReceiverKindEnum() throws IOException {
    skipByte();
  }

  @Nonnull
  public DispatchKind readDispatchKindEnum() throws IOException {
    return DispatchKindIdHelper.getValue(readByte());
  }

  public void skipDispatchKindEnum() throws IOException {
    skipByte();
  }

  @CheckForNull
  public String readString() throws IOException {
    return tokenizer.readString();
  }

  public void skipString() throws IOException {
    tokenizer.skipString();
  }

  @CheckForNull
  public byte[] readBuffer() throws IOException {
    return tokenizer.readBuffer();
  }

  public void skipBuffer() throws IOException {
    tokenizer.skipBuffer();
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

  public void skipIds() throws IOException {
    tokenizer.readOpen();
    int length = readInt();
    for (int i = 0; i < length; i++) {
      skipId();
    }
    tokenizer.readClose();
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

  public void skipCatchBlockIds() throws IOException {
    if (tokenizer.readOpenCatchBlockIdAdd()) {
      int length = tokenizer.readInt();
      for (int i = 0; i < length; i++) {
        skipId();
      }
      tokenizer.readCloseCatchBlockId();
    }
    if (tokenizer.readOpenCatchBlockIdRemove()) {
      int length = tokenizer.readInt();
      for (int i = 0; i < length; i++) {
        skipId();
      }
      tokenizer.readCloseCatchBlockId();
    }
  }

  public <T extends NNode> void skipNode()
      throws IOException, JayceFormatException {

    skipCurrentFileName();
    skipCurrentLine();

    skipCatchBlockIds();

    Token token = tokenizer.next();

    if (token == Token.NULL) {
      return;
    }

    tokenizer.readOpen();

    skipNodeInternal(token);
  }

  private <T extends NNode> void skipNodeInternal(@Nonnull Token token)
      throws IOException, JayceFormatException {

    try {

      token.skip(this);

      if (nodeLevel != NodeLevel.TYPES) {
        if (token.hasSourceInfo()) {
          skipCurrentFileName();
          skipCurrentLine();
        }
        tokenizer.readClose();
      }

    } catch (InvalidTokenException e) {
      throw new ParseException(
          "Unexpected token " + token.toString() + " while expecting node.", e);
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

      Percent statistic = null;
      if (token == Token.METHOD_BODY) {
        statistic = tracer.getStatistic(SKIPPED_BODY);
      }

      if (!nodeLevel.keep(token.getNodeLevel())) {

        skipNodeInternal(token);

        if (statistic != null) {
          statistic.addTrue();
        }
        return null;
      }

      node = token.newNode();

      if (node instanceof NDeclaredType) {
        tracer.getStatistic(SKIPPED_TYPE_STRUCTURE).add(nodeLevel == NodeLevel.TYPES);
      }

      if (statistic != null) {
        statistic.addFalse();
      }


    } catch (InvalidTokenException e) {
      throw new ParseException(
          "Unexpected token " + token.toString() + " while expecting node.", e);
    }

    assert node != null;

    if (!nodeClass.isAssignableFrom(node.getClass())) {
      throw new JayceFormatException("Unexpected node " + node.getClass().getSimpleName() + ", "
          + nodeClass.getSimpleName() + " was expected.");
    }

    if (node instanceof HasCatchBlockIds) {
      ((HasCatchBlockIds) node).setCatchBlockIds(new ArrayList<String>(currentCatchBlockList));
    }
    /* readContent can stop in the middle of the node data when nodeLevel is NodeLevel.TYPES
     * meaning we can't read anything after and we have to skip source info.
     */
    node.readContent(this);
    if (nodeLevel != NodeLevel.TYPES) {

      if (node instanceof HasSourceInfo) {
        fileName = readCurrentFileName();
        int endLine = readCurrentLine();
        if (fileName == null) {
          assert startLine == 0 && endLine == 0;
          ((HasSourceInfo) node).setSourceInfos(SourceInfo.UNKNOWN);
        } else {
          assert fileName != null;
          ((HasSourceInfo) node).setSourceInfos(
              sif.create(/* startCol= */ 0, /* endCol */ 0, startLine, endLine, fileName));
        }
      }

      assert !(node instanceof NMethod) || currentCatchBlockList.isEmpty();
      tokenizer.readClose();
    }

    return (T) node;
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
      }
    }
    tokenizer.readClose();
    return nodes;

  }

  public <T extends NNode> void skipNodes() throws IOException,
      JayceFormatException {
    tokenizer.readOpen();
    int length = readInt();
    for (int i = 0; i < length; i++) {
      skipNode();
    }
    tokenizer.readClose();
  }

  public int readInt() throws IOException {
    return tokenizer.readInt();
  }

  public void skipInt() throws IOException {
    tokenizer.skipInt();
  }

  public byte readByte() throws IOException {
    return tokenizer.readByte();
  }

  public void skipByte() throws IOException {
    tokenizer.skipByte();
  }

  public boolean readBoolean() throws IOException {
    return tokenizer.readBoolean();
  }

  public void skipBoolean() throws IOException {
    tokenizer.skipBoolean();
  }

  public long readLong() throws IOException {
    return tokenizer.readLong();
  }

  public void skipLong() throws IOException {
    tokenizer.skipLong();
  }

  public short readShort() throws IOException {
    return tokenizer.readShort();
  }

  public void skipShort() throws IOException {
    tokenizer.skipShort();
  }

  public char readChar() throws IOException {
    return tokenizer.readChar();
  }

  public void skipChar() throws IOException {
    tokenizer.skipChar();
  }

  public float readFloat() throws IOException {
    return tokenizer.readFloat();
  }

  public void skipFloat() throws IOException {
    tokenizer.skipFloat();
  }

  public double readDouble() throws IOException {
    return tokenizer.readDouble();
  }

  public void skipDouble() throws IOException {
    tokenizer.skipDouble();
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
