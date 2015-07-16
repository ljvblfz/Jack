/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jill.backend.jayce;

import com.android.jill.utils.enums.DispatchKindIdHelper;
import com.android.jill.utils.enums.FieldRefKindIdHelper;
import com.android.jill.utils.enums.MethodKindIdHelper;
import com.android.jill.utils.enums.ReceiverKindIdHelper;
import com.android.jill.utils.enums.RetentionPolicyIdHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *  Jayce writer.
 */
public class JayceWriter {

  protected final Stack<JayceOutputStream> writers = new Stack<JayceOutputStream>();
  private final Stack<ByteArrayOutputStream> outputStreams = new Stack<ByteArrayOutputStream>();
  private final Stack<Integer> nodeCounters = new Stack<Integer>();

  @Nonnull
  private final List<String> currentCatchBlockList = new ArrayList<String>();

  public JayceWriter(@Nonnull OutputStream out) {
    writers.push(new JayceOutputStream(out));
    nodeCounters.push(Integer.valueOf(0));
  }

  public void writeBoolean(boolean value)  throws IOException {
    writers.peek().writeBoolean(value);
  }

  private void writeIntInternal(int value) throws IOException {
    writers.peek().writeInt(value);
  }

  public void writeInt(int value) throws IOException {
    writeIntInternal(value);
    writeSpace();
  }

  public void writeTrimmedInt(int value) throws IOException {
    writeIntInternal(value);
  }

  private void writeLongInternal(long value) throws IOException {
    writers.peek().writeLong(value);
  }

  public void writeLong(long value) throws IOException {
    writeLongInternal(value);
    writeSpace();
  }

  public void writeByte(byte value) throws IOException {
    writers.peek().writeByte(value);
    writeSpace();
  }

  public void writeShort(short value) throws IOException {
    writers.peek().writeShort(value);
    writeSpace();
  }

  public void writeChar(char value) throws IOException {
    writers.peek().writeChar(value);
    writeSpace();
  }

  public void writeFloat(float value) throws IOException {
    writeIntInternal(Float.floatToRawIntBits(value));
    writeSpace();
  }

  public void writeDouble(double value) throws IOException {
    writeLongInternal(Double.doubleToRawLongBits(value));
    writeSpace();
  }

  public void writeKeyword(@Nonnull Token token) throws IOException {
    writers.peek().writeByte(token.ordinal());
    nodeCounters.push(Integer.valueOf(nodeCounters.pop().intValue() + 1));
  }

  public void writeToken(@Nonnull Token token) throws IOException {
    writers.peek().writeByte(token.ordinal());
  }

  @SuppressWarnings("unused")
  public void writeOpen() throws IOException {
    nodeCounters.push(Integer.valueOf(0));
  }

  public void writeClose() throws IOException {
    writeToken(Token.RPARENTHESIS);
    nodeCounters.pop();
  }

  public void writeOpenNodeList() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    outputStreams.push(baos);
    writers.push(new JayceOutputStream(baos));
    nodeCounters.push(Integer.valueOf(0));
  }

  public void writeCloseNodeList() throws IOException {
    JayceOutputStream w = writers.pop();
    w.flush();
    ByteArrayOutputStream baos = outputStreams.pop();
    writeIntInternal(nodeCounters.pop().intValue());
    writers.peek().write(baos.toByteArray());
    w.close();
    writeToken(Token.RPARENTHESIS);
  }

  public void writeRetentionPolicyEnum(@Nonnull Enum<?> enumValue) throws IOException {
    writeByte(RetentionPolicyIdHelper.getId(enumValue));
  }

  public void writeFieldRefKindEnum(@Nonnull Enum<?> enumValue) throws IOException {
    writeByte(FieldRefKindIdHelper.getId(enumValue));
  }

  public void writeMethodKindEnum(@Nonnull Enum<?> enumValue) throws IOException {
    writeByte(MethodKindIdHelper.getId(enumValue));
  }

  public void writeReceiverKindEnum(@Nonnull Enum<?> enumValue) throws IOException {
    writeByte(ReceiverKindIdHelper.getId(enumValue));
  }

  public void writeDispatchKindEnum(@Nonnull Enum<?> enumValue) throws IOException {
    writeByte(DispatchKindIdHelper.getId(enumValue));
  }

  public void writeIds(@Nonnull List<String> list) throws IOException {
    writeOpen();
    writeIntInternal(list.size());
    for (String id : list) {
      writeId(id);
    }

    writeClose();
  }

  public void writeCatchBlockIds(@Nonnull Set<String> list) throws IOException {
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
      writeTrimmedInt(addedIdsSize);
      for (int i = 0; i < addedIdsSize; i++) {
        writeString(addedIds.get(i));
      }
      writeCloseCatchBlockIds();
    }

    if (removedIdsSize > 0) {
      writeOpenRemoveCatchBlockIds();
      writeTrimmedInt(removedIdsSize);
      for (int i = 0; i < removedIdsSize; i++) {
        writeString(removedIds.get(i));
      }
      writeCloseCatchBlockIds();
    }
    currentCatchBlockList.addAll(addedIds);
  }

  public void clearCatchBlockIds() {
    currentCatchBlockList.clear();
  }

  public boolean isCurrentCatchBlockListEmpty() {
    return currentCatchBlockList.isEmpty();
  }

  public void writeId(@CheckForNull String id)  throws IOException {
    writeStringInternal(id);
  }

  private void writeStringInternal(@CheckForNull String string) throws IOException {
    writers.peek().writeUTF(string);
  }

  public void writeString(@CheckForNull String string)  throws IOException {
    writeStringInternal(string);
  }

  public void writeNull()  throws IOException {
    writeToken(Token.NULL);
    writeSpace();
    nodeCounters.push(Integer.valueOf(nodeCounters.pop().intValue() + 1));
  }

  @SuppressWarnings("unused")
  protected void writeSpace()  throws IOException{
  }

  public void writeFileName(@CheckForNull String fileName) throws IOException {
    writeOpenFileName();
    writeStringInternal(fileName);
    writeCloseFileName();
  }

  private void writeOpenFileName() throws IOException {
    writeToken(Token.SHARP);
  }

  @SuppressWarnings("unused")
  private void writeCloseFileName()  throws IOException{
  }

  public void writeCurrentLineInfo(int lineNumber)
      throws IOException {
    writeOpenLineInfo();
    writeIntInternal(lineNumber);
    writeCloseLineInfo();
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

  public void flush() throws IOException {
    writers.peek().flush();
  }
}
