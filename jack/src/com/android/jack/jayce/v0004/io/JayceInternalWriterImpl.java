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

import com.android.jack.JackEventType;
import com.android.jack.ir.ast.FieldKind;
import com.android.jack.ir.ast.JMethodCall.DispatchKind;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.jayce.JayceInternalWriter;
import com.android.jack.jayce.v0004.NNode;
import com.android.jack.jayce.v0004.NodeFactory;
import com.android.jack.jayce.v0004.Version;
import com.android.jack.jayce.v0004.nodes.HasCatchBlockIds;
import com.android.jack.jayce.v0004.nodes.HasSourceInfo;
import com.android.jack.jayce.v0004.nodes.NMethod;
import com.android.jack.jayce.v0004.nodes.NMethodCall.ReceiverKind;
import com.android.jack.jayce.v0004.util.DispatchKindIdHelper;
import com.android.jack.jayce.v0004.util.FieldRefKindIdHelper;
import com.android.jack.jayce.v0004.util.MethodKindIdHelper;
import com.android.jack.jayce.v0004.util.ReceiverKindIdHelper;
import com.android.jack.jayce.v0004.util.RetentionPolicyIdHelper;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.location.HasLocation;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jayce internal writer implementation.
 */
public class JayceInternalWriterImpl implements JayceInternalWriter {

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final JayceOutputStream out;

  @CheckForNull
  private String currentFileName;

  @Nonnegative
  private int currentLineNumber;

  @Nonnull
  private final  List<String> currentCatchBlockList = new ArrayList<String>();

  @Nonnull
  private final HasLocation locationProvider;

  public JayceInternalWriterImpl(@Nonnull OutputStream out, @Nonnull HasLocation locationProvider) {
    this.out = new JayceOutputStream(out);
    this.locationProvider = locationProvider;
  }

  public void writeNode(@CheckForNull NNode node) throws IOException {
    if (node == null) {
      writeNull();
    } else {
      writeSourceInfoBegin(node);
      writeCatchBlockIds(node);
      writeToken(node.getToken());
      writeOpen();
      node.writeContent(this);
      writeSourceInfoEnd(node);
      assert !(node instanceof NMethod) || currentCatchBlockList.isEmpty();
      writeClose();
    }
  }

  private void writeSourceInfoBegin(@Nonnull NNode node) throws IOException {
    if (node instanceof HasSourceInfo) {
      SourceInfo sourceInfo = ((HasSourceInfo) node).getSourceInfos();
      if (sourceInfo == SourceInfo.UNKNOWN) {
        writeUnknowDebug();
      } else {
        writeFileNameIfDifferentFromCurrent(sourceInfo.getFileName());
        writeLineIfDifferentFromCurrent(sourceInfo.getStartLine());
      }
    }
  }

  private void writeSourceInfoEnd(@Nonnull NNode node) throws IOException {
    if (node instanceof HasSourceInfo) {
      SourceInfo sourceInfo = ((HasSourceInfo) node).getSourceInfos();
      if (sourceInfo == SourceInfo.UNKNOWN) {
        writeUnknowDebug();
      } else {
        writeFileNameIfDifferentFromCurrent(sourceInfo.getFileName());
        writeLineIfDifferentFromCurrent(sourceInfo.getEndLine());
      }
    }
  }

  public void writeIds(@Nonnull List<String> list) throws IOException {
    writeOpen();

    writeTrimmedInt(list.size());

    for (String id : list) {
      writeId(id);
    }

    writeClose();
  }

  public void writeCatchBlockIds(@CheckForNull NNode node) throws IOException {
    if (node instanceof HasCatchBlockIds) {
      List<String> list = ((HasCatchBlockIds) node).getCatchBlockIds();
      List<String> removedIds = new ArrayList<String>(currentCatchBlockList.size());
      List<String> addedIds = new ArrayList<String>(list.size());

      for (String s : currentCatchBlockList) {
        removedIds.add(s);
      }
      for (String s : list) {
        addedIds.add(s);
      }

      // intersection(current, list)
      currentCatchBlockList.retainAll(list);
      // current \ intersection(current, list)
      removedIds.removeAll(currentCatchBlockList);
      // list \ intersection(current, list)
      addedIds.removeAll(currentCatchBlockList);

      int addedIdsSize = addedIds.size();
      int removedIdsSize = removedIds.size();

      if (addedIdsSize > 0) {
        writeOpenAddCatchBlockIds();
        writeInt(addedIdsSize);
        for (int i = 0; i < addedIdsSize; i++) {
          writeString(addedIds.get(i));
        }
        writeCloseCatchBlockIds();
      }

      if (removedIdsSize > 0) {
        writeOpenRemoveCatchBlockIds();
        writeInt(removedIdsSize);
        for (int i = 0; i < removedIdsSize; i++) {
          writeString(removedIds.get(i));
        }
        writeCloseCatchBlockIds();
      }
      currentCatchBlockList.addAll(addedIds);
    }
  }

  public void writeNodes(@Nonnull Collection<? extends NNode> nodes) throws IOException {
    writeOpen();

    writeTrimmedInt(nodes.size());

    for (Iterator<? extends NNode> iterator = nodes.iterator(); iterator.hasNext();) {
      writeNode(iterator.next());
    }

    writeClose();
  }

  public void writeInt(int value) throws IOException {
    writeTrimmedInt(value);
    writeSpace();
  }

  private void writeTrimmedInt(int value) throws IOException {
    out.writeInt(value);
  }

  public void writeBoolean(boolean value)  throws IOException {
    out.writeBoolean(value);
  }

  public void writeLong(long value) throws IOException {
    writeTrimmedLong(value);
    writeSpace();
  }

  private void writeTrimmedLong(long value) throws IOException {
    out.writeLong(value);
  }

  public void writeByte(byte value) throws IOException {
    out.writeByte(value);
    writeSpace();
  }

  public void writeShort(short value) throws IOException {
    out.writeShort(value);
    writeSpace();
  }

  public void writeChar(char value) throws IOException {
    out.writeChar(value);
    writeSpace();
  }

  public void writeFloat(float value) throws IOException {
    writeTrimmedInt(Float.floatToRawIntBits(value));
    writeSpace();
  }

  public void writeDouble(double value) throws IOException {
    writeTrimmedLong(Double.doubleToRawLongBits(value));
    writeSpace();
  }

  public void writeId(@CheckForNull String id)  throws IOException {
    writeString(id);
  }

  public void writeRetentionPolicyEnum(@Nonnull JRetentionPolicy enumValue) throws IOException {
    writeByte(RetentionPolicyIdHelper.getId(enumValue));
  }

  public void writeFieldRefKindEnum(@Nonnull FieldKind enumValue) throws IOException {
    writeByte(FieldRefKindIdHelper.getId(enumValue));
  }

  public void writeMethodKindEnum(@Nonnull MethodKind enumValue) throws IOException {
    writeByte(MethodKindIdHelper.getId(enumValue));
  }

  public void writeReceiverKindEnum(@Nonnull ReceiverKind enumValue) throws IOException {
    writeByte(ReceiverKindIdHelper.getId(enumValue));
  }

  public void writeDispatchKindEnum(@Nonnull DispatchKind enumValue) throws IOException {
    writeByte(DispatchKindIdHelper.getId(enumValue));
  }

  public void writeString(@CheckForNull String string)  throws IOException {
    out.writeUTF(string);
  }

  public void writeBuffer(@CheckForNull byte[] b)  throws IOException {
    out.writeBuffer(b);
  }

  public void writeFileNameIfDifferentFromCurrent(@Nonnull String fileName)
      throws IOException {
   if (!fileName.equals(currentFileName)) {
      writeCurrentFileName(fileName);
    }
  }

  private void writeUnknowDebug()  throws IOException {
    if (currentFileName != null) {
      writeOpenFileName();
      writeString(null);
      writeCloseFileName();
      currentFileName = null;
      currentLineNumber = 0;
    }
  }

  private void writeCurrentFileName(@CheckForNull String fileName)  throws IOException {
    writeOpenFileName();
    writeString(fileName);
    writeCloseFileName();
    currentFileName = fileName;
  }

  public void writeLineIfDifferentFromCurrent(@Nonnegative int lineNumber) throws IOException {
    if (lineNumber != currentLineNumber) {
      writeCurrentLine(lineNumber);
    }
  }

  public void writeCurrentLine(@Nonnegative int lineNumber) throws IOException {
    writeOpenLineInfo();
    writeTrimmedInt(lineNumber);
    writeCloseLineInfo();
    currentLineNumber = lineNumber;
  }

  private void writeNull()  throws IOException {
    writeToken(Token.NULL);
    writeSpace();
  }

  @SuppressWarnings("unused")
  private void writeSpace()  throws IOException{
  }

  private void writeToken(@Nonnull Token token) throws IOException {
    out.writeByte(token.ordinal());
  }

  @SuppressWarnings("unused")
  private void writeOpen()  throws IOException{
  }

  private void writeClose() throws IOException {
    writeToken(Token.RPARENTHESIS);
  }

  private void writeOpenFileName() throws IOException {
    writeToken(Token.SHARP);
  }

  @SuppressWarnings("unused")
  private void writeCloseFileName()  throws IOException{
  }

  private void writeOpenLineInfo() throws IOException {
    writeToken(Token.LBRACKET);
  }

  @SuppressWarnings("unused")
  private void writeCloseLineInfo()  throws IOException{
  }

  private void writeOpenAddCatchBlockIds() throws IOException {
    writeToken(Token.LCURLY_ADD);
  }

  private void writeOpenRemoveCatchBlockIds() throws IOException {
    writeToken(Token.LCURLY_REMOVE);
  }

  @SuppressWarnings("unused")
  private void writeCloseCatchBlockIds()  throws IOException{
  }

  @Override
  public void write(@Nonnull JNode jNode) throws CannotWriteException {
    try (Event eventWriting = tracer.open(JackEventType.NNODE_WRITING)) {
      ImportHelper importHelper = new ImportHelper(new NodeFactory());
      NNode nNode;
      try (Event eventConvert = tracer.open(JackEventType.JNODE_TO_NNODE_CONVERSION)) {
        nNode = importHelper.load(jNode);
      }

      try {
        writeNode(nNode);
      } catch (IOException e) {
        throw new CannotWriteException(locationProvider, e);
      }
    }
  }

  @Override
  public int getCurrentMinor() {
    return Version.CURRENT_MINOR;
  }

  @Override
  public void close() throws CannotCloseException {
    try {
      out.close();
    } catch (IOException e) {
      throw new CannotCloseException(locationProvider, e);
    }
  }
}
