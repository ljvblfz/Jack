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

package com.android.jill.frontend.java;

import com.android.jill.JillException;
import com.android.jill.Options;
import com.android.jill.backend.jayce.JayceWriter;
import com.android.jill.backend.jayce.Token;
import com.android.jill.frontend.java.analyzer.JillAnalyzer;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Method body writer.
 */
public class MethodBodyWriter extends JillWriter implements Opcodes {

  @Nonnull
  private final Map<String, Variable> nameToVar = new HashMap<String, Variable>();

  @Nonnull
  private final Map<Variable, Variable> parameter2Var = new LinkedHashMap<Variable, Variable>();

  public static final int CONSTRUCTOR  = 0x10000;

  /**
   * Kinds of method call dispatch.
   */
  public enum DispatchKind {
    VIRTUAL,
    DIRECT
  }

  /**
   * Kinds of method.
   */
  public enum MethodKind {
    STATIC,
    INSTANCE_NON_VIRTUAL,
    INSTANCE_VIRTUAL
  }

  /**
   * Kinds of method call receiver.
   */
  public enum MethodCallReceiverKind {
    CLASS,
    INTERFACE
  }

  /**
   * kinds of field reference.
   */
  public enum FieldRefKind {
    INSTANCE,
    STATIC;
  }

  private static class Case {
    @Nonnull
    LabelNode labelNode;
    @CheckForNull
    Integer key;
    @Nonnull
    String caseId;

    public Case(
        @Nonnull LabelNode labelNode, @Nonnegative int switchIdx, @CheckForNull Integer key) {
      this.labelNode = labelNode;
      this.key = key;
      caseId = switchIdx + "_" + (this.key != null ? this.key : "default");
    }
  }

  private static class CmpOperands{
    @Nonnegative
    int opcode;
    @Nonnull
    Variable lhs;
    @Nonnull
    Variable rhs;

    public CmpOperands(@Nonnegative int opcode, @Nonnull Variable lhs, @Nonnull Variable rhs) {
      this.opcode = opcode;
      this.lhs = lhs;
      this.rhs = rhs;
    }
  }

  @Nonnull
  private final HashMap<Variable, CmpOperands> cmpOperands =
      new HashMap<Variable, MethodBodyWriter.CmpOperands>();

  @Nonnull
  private final AnnotationWriter annotWriter;

  @Nonnegative
  private static final int NO_MODIFIER = 0;

  private static final int TOP_OF_STACK = -1;

  @Nonnull
  private final Set<String> currentCatchList = new HashSet<String>();

  @Nonnegative
  private int currentLine = 0;

  @Nonnull
  private final ClassNode currentClass;

  @Nonnull
  private final MethodNode currentMethod;

  @Nonnull
  private final Analyzer<BasicValue> analyzer;

  @Nonnegative
  private int unusedVarCount = 0;

  @Nonnegative
  private int currentPc = 0;

  private int startLine = -1;
  private int endLine = -1;

  @Nonnull
  private final Options options;

  @Nonnull
  private final Map<TryCatchBlockNode, Variable> catchBlockToCatchedVariable =
    new HashMap<TryCatchBlockNode, Variable>();

  public MethodBodyWriter(@Nonnull JayceWriter writer,
      @Nonnull AnnotationWriter annotWriter,
      @Nonnull ClassNode cn, @Nonnull MethodNode mn,
      @Nonnull SourceInfoWriter sourceInfoWriter,
      @Nonnull Options options) {
    super(writer, sourceInfoWriter);
    this.annotWriter = annotWriter;
    this.options = options;
    currentClass = cn;
    BasicInterpreter bi = new JillAnalyzer();
    analyzer = new Analyzer<BasicValue>(bi);

    if (mn.instructions.size() != 0) {
      currentMethod = getMethodWithoutJSR(mn);

      try {
        analyzer.analyze(currentClass.name, currentMethod);

        removeDeadCode();

        analyzer.analyze(currentClass.name, currentMethod);
      } catch (AnalyzerException e) {
        throw new JillException("Variable analyser fails.", e);
      }
    } else {
      currentMethod = mn;
    }
  }

  public void write() throws IOException {
    if (AsmHelper.isAnnotation(currentClass)) {
      writeAnnotationMethod();
    } else if (AsmHelper.isConstructor(currentMethod)) {
      writeConstructor();
    } else {
      writeMethod();
    }
  }

  private void writeConstructor() throws IOException {
    computeStartAndEndLine();
    sourceInfoWriter.writeDebugBegin(currentClass, startLine);
    writer.writeKeyword(Token.CONSTRUCTOR);
    writer.writeOpen();
    writeParameters();
    writer.writeInt(AsmHelper.getModifiers(currentMethod));
    annotWriter.writeAnnotations(currentMethod);
    writeMethodBody();
    writer.writeOpenNodeList(); // Markers
    writeOriginalTypeInfoMarker();
    writeThrownExceptionMarker();
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(currentClass, endLine);
    writer.writeClose();
  }

  private void writeMethod() throws IOException {
    computeStartAndEndLine();
    sourceInfoWriter.writeDebugBegin(currentClass, startLine);
    writer.writeKeyword(Token.METHOD);
    writer.writeOpen();
    writer.writeString(currentMethod.name);
    writer.writeId(Type.getReturnType(currentMethod.desc).getDescriptor());
    writeParameters();

    MethodKind methodKind;
    if (AsmHelper.isStatic(currentMethod)) {
      methodKind = MethodKind.STATIC;
    } else if (AsmHelper.isConstructor(currentMethod) || AsmHelper.isPrivate(currentMethod)) {
      methodKind = MethodKind.INSTANCE_NON_VIRTUAL;
    } else {
      methodKind = MethodKind.INSTANCE_VIRTUAL;
    }
    writer.writeMethodKindEnum(methodKind);

    writer.writeInt(AsmHelper.isStaticInit(currentMethod) ? AsmHelper.getModifiers(currentMethod)
        | CONSTRUCTOR : AsmHelper.getModifiers(currentMethod));
    annotWriter.writeAnnotations(currentMethod);
    writeMethodBody();
    writer.writeOpenNodeList(); // Markers
    writeOriginalTypeInfoMarker();
    writeThrownExceptionMarker();
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(currentClass, endLine);
    writer.writeClose();
  }

  private void writeAnnotationMethod() throws IOException {
    computeStartAndEndLine();
    sourceInfoWriter.writeDebugBegin(currentClass, startLine);
    writer.writeKeyword(Token.ANNOTATION_METHOD);
    writer.writeOpen();
    writer.writeString(currentMethod.name);
    writer.writeId(Type.getReturnType(currentMethod.desc).getDescriptor());
    writer.writeInt(AsmHelper.getModifiers(currentMethod));
    annotWriter.writeAnnotations(currentMethod);
    if (currentMethod.annotationDefault != null) {
      annotWriter.writeValue(currentMethod.annotationDefault);
    } else {
      writer.writeNull();
    }
    writer.writeOpenNodeList(); // Markers
    writeOriginalTypeInfoMarker();
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(currentClass, endLine);
    writer.writeClose();
  }

  private void writeOriginalTypeInfoMarker() throws IOException {
    if (currentMethod.signature != null) {
      writer.writeKeyword(Token.GENERIC_SIGNATURE);
      writer.writeOpen();
      writer.writeString(currentMethod.signature);
      writer.writeClose();
    } else {
      writer.writeNull();
    }
  }

  private void writeThrownExceptionMarker() throws IOException {
    if (currentMethod.exceptions != null && !currentMethod.exceptions.isEmpty()) {
      writer.writeKeyword(Token.THROWN_EXCEPTION);
      writer.writeOpen();
      writer.writeIds(AsmHelper.getDescriptorsFromInternalNames(currentMethod.exceptions));
      writer.writeClose();
    }
  }

  @Nonnull
  private MethodNode getMethodWithoutJSR(@Nonnull MethodNode mn) {
    JSRInlinerAdapter jsrInliner =
        new JSRInlinerAdapter(null, mn.access, mn.name, mn.desc, mn.signature,
            mn.exceptions.toArray(new String[mn.exceptions.size()]));

    mn.accept(jsrInliner);

    return jsrInliner;
  }

  private void writeMethodBody() throws IOException {
    currentCatchList.clear();
    writer.clearCatchBlockIds();

    if (AsmHelper.isNative(currentMethod)) {
      writeNativeMethodBody();
    } else if (AsmHelper.isAbstract(currentMethod)) {
      writer.writeNull();
    } else {
      createCaughtVariables();
      currentLine = startLine;
      writeJavaMethodBody();
    }

    assert writer.isCurrentCatchBlockListEmpty();
  }

  private void computeStartAndEndLine() {
    for (AbstractInsnNode insn : currentMethod.instructions.toArray()) {
      if (insn instanceof LineNumberNode) {
        LineNumberNode lnn = (LineNumberNode) insn;

        if (startLine == -1) {
          startLine = lnn.line;
          endLine = lnn.line + 1;
          continue;
        }

        if (lnn.line < startLine) {
          startLine = lnn.line;
        } else if (lnn.line > endLine) {
          endLine = lnn.line;
        }
      }
    }
  }

  private void createCaughtVariables() {
    for (TryCatchBlockNode tryCatchNode : currentMethod.tryCatchBlocks) {
      Variable declaringCatchVariable = null;
      Type caughtType;
      if (tryCatchNode.type == null) {
        // Jack represents finally by a catch on java.lang.Object.
        caughtType = Type.getType(Object.class);
      } else {
        // If there are multi catches, it is not possible to compute precisely the common type of
        // exceptions without having the full classpath and by loading all classes. Jill uses
        // Throwable as common type even when a more precise type is known.
        // This type will be cast with a reinterpret cast to the right type when it will be used.
        caughtType = Type.getType(Throwable.class);
      }
      String id = "-e_" + (unusedVarCount++);
      declaringCatchVariable = new Variable(id, id, caughtType, null);
      catchBlockToCatchedVariable.put(tryCatchNode, declaringCatchVariable);
    }
  }

  private void writeNativeMethodBody() throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.NATIVE_METHOD_BODY);
    writer.writeOpen();
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  private void writeJavaMethodBody() throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, startLine);
    writer.writeKeyword(Token.METHOD_BODY);
    writer.writeOpen();
    writeLocals();
    writeBody();
    sourceInfoWriter.writeDebugEnd(currentClass, endLine);
    writer.writeClose();
  }

  private void writeBody() throws IOException {

    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.BLOCK);
    writer.writeOpen();
    writer.writeOpenNodeList();

    if (currentMethod.instructions.size() == 0) {
      if (options.isTolerant()) {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.THROW_STATEMENT);
        writer.writeOpen();
        writer.writeKeyword(Token.NEW_INSTANCE);
        writer.writeOpen();
        // Type of created object
        writer.writeId("Ljava/lang/AssertionError;");
        // Empty argument types
        writer.writeIds(Collections.<String>emptyList());
        // No arguments
        writer.writeOpenNodeList();
        writer.writeCloseNodeList();
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
      } else {
        throw new JillException("Method should have instructions.");
      }
    } else {
      for (Map.Entry<Variable, Variable> entry : parameter2Var.entrySet()) {
        Variable p = entry.getKey();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeLocalRef(entry.getValue());
        if (p.getType() == Type.BOOLEAN_TYPE) {
          writeCastOperation(Token.REINTERPRETCAST_OPERATION, p, Type.INT_TYPE.getDescriptor());
        } else {
          writeLocalRef(p);
        }
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
      }

      Frame<BasicValue>[] frames = analyzer.getFrames();

      for (int insnIdx = 0; insnIdx < currentMethod.instructions.size(); insnIdx++) {
        currentPc = insnIdx;
        AbstractInsnNode insn = currentMethod.instructions.get(insnIdx);
        Frame<BasicValue> currentFrame = frames[insnIdx];
        // There's no next frame if insn is a return, and the last instruction.
        Frame<BasicValue> nextFrame = (insnIdx < frames.length - 1) ? frames[insnIdx + 1] : null;

        if (insn instanceof JumpInsnNode) {
          writeInsn(currentFrame, (JumpInsnNode) insn, insnIdx);
        } else if (insn instanceof LdcInsnNode) {
          assert nextFrame != null;
          writeInsn(nextFrame, (LdcInsnNode) insn);
        } else if (insn instanceof InsnNode) {
          writeInsn(currentFrame, nextFrame, (InsnNode) insn);
        } else if (insn instanceof VarInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (VarInsnNode) insn);
        } else if (insn instanceof LabelNode) {
          computeCatchList((LabelNode) insn);
          writeCatchBlock((LabelNode) insn, insnIdx, frames);
          writeLabelInsn(insnIdx);
        } else if (insn instanceof FieldInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (FieldInsnNode) insn);
        } else if (insn instanceof MethodInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (MethodInsnNode) insn);
        } else if (insn instanceof LineNumberNode) {
          currentLine = ((LineNumberNode) insn).line;
        } else if (insn instanceof FrameNode) {
          // Nothing to do.
        } else if (insn instanceof TypeInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (TypeInsnNode) insn);
        } else if (insn instanceof TableSwitchInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (TableSwitchInsnNode) insn, insnIdx);
        } else if (insn instanceof LookupSwitchInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (LookupSwitchInsnNode) insn, insnIdx);
        } else if (insn instanceof IntInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (IntInsnNode) insn);
        } else if (insn instanceof IincInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (IincInsnNode) insn);
        } else if (insn instanceof MultiANewArrayInsnNode) {
          assert nextFrame != null;
          writeInsn(currentFrame, nextFrame, (MultiANewArrayInsnNode) insn);
        } else {
          throw new JillException("Unsupported instruction.");
        }
      }

      // Current solution for comparison requires its result to be consumed by an "if"
      if (!cmpOperands.isEmpty()) {
        throw new AssertionError("A comparison has not been followed by an if");
      }
    }

    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeCatchBlock(@Nonnull LabelNode labelNode, @Nonnegative int labelIdx,
      @Nonnull Frame<BasicValue>[] frames) throws IOException {
    for (TryCatchBlockNode tryCatchNode : currentMethod.tryCatchBlocks) {
      if (tryCatchNode.handler == labelNode) {
        // Always create a variable that will be typed with catched exception. Reuse computed
        // variable is not possible since type could be lost due to merging.
        Variable declaringCatchVariable = catchBlockToCatchedVariable.get(tryCatchNode);

        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.CATCH_BLOCK);
        writer.writeOpen();
        writer.writeId(getCatchId(tryCatchNode.handler));

        // Take into account multi catches by computing the list of caught types for this handler
        List<String> ids = new ArrayList<String>();
        if (tryCatchNode.type == null) {
          // Jack represents finally by a catch on java.lang.Object.
          ids.add(Type.getType(Object.class).getDescriptor());
        } else {
          ids.add(Type.getObjectType(tryCatchNode.type).getDescriptor());
          for (TryCatchBlockNode tryCatchNode2 : currentMethod.tryCatchBlocks) {
            if (labelNode == tryCatchNode2.handler && tryCatchNode != tryCatchNode2
                && !tryCatchNode.type.equals(tryCatchNode2.type)) {
              ids.add(Type.getObjectType(tryCatchNode2.type).getDescriptor());
            }
          }
        }
        writer.writeIds(ids);

        writeLocal(declaringCatchVariable);

        writer.writeOpenNodeList();

        if (frames[labelIdx] != null) {
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeCatchBlockIds(currentCatchList);
          writer.writeKeyword(Token.EXPRESSION_STATEMENT);
          writer.writeOpen();
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeKeyword(Token.ASG_OPERATION);
          writer.writeOpen();
          writeStackAccess(frames[labelIdx], TOP_OF_STACK);
          writeLocalRef(declaringCatchVariable);
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();
        }
        writeGoto(tryCatchNode.handler);

        writer.writeCloseNodeList();

        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break; // Write catch block only one time even if the handler is used severals times.
      }
    }
  }

  private void computeCatchList(@Nonnull LabelNode labelNode) {
    for (TryCatchBlockNode tryCatchNode : currentMethod.tryCatchBlocks) {
      String id = getCatchId(tryCatchNode.handler);
      if (tryCatchNode.start == labelNode) {
        currentCatchList.add(id);
      } else if (tryCatchNode.end == labelNode) {
        currentCatchList.remove(id);
      }
    }
  }

  @Nonnull
  private String getCatchId(@Nonnull LabelNode labelNode) {
    int insnIndex = currentMethod.instructions.indexOf(labelNode);
    return Integer.toString(insnIndex) + "-catch";
  }

  private void writeLabelInsn(@Nonnegative int insnIdx)
        throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.LABELED_STATEMENT);
    writer.writeOpen();
    String id = Integer.toString(insnIdx);
    writer.writeString(id);
    writer.writeId(id);
    writeEmptyBlock();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeEmptyBlock() throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.BLOCK);
    writer.writeOpen();
    writer.writeOpenNodeList();
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull IincInsnNode iincInsn) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();
    writeLocalAccess(nextFrame, iincInsn.var);
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ADD_OPERATION);
    writer.writeOpen();
    writeLocalAccess(frame, iincInsn.var);
    writeValue(iincInsn.incr);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull IntInsnNode intInsn) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();
    writeStackAccess(nextFrame, TOP_OF_STACK);

    switch (intInsn.getOpcode()) {
      case BIPUSH: {
        writeValue(intInsn.operand);
        break;
      }
      case SIPUSH: {
        writeValue(intInsn.operand);
        break;
      }
      case NEWARRAY: {

        switch (intInsn.operand) {
          case T_BOOLEAN: {
            writeNewArray(frame, "[Z", 1);
            break;
          }
          case T_CHAR: {
            writeNewArray(frame, "[C", 1);
            break;
          }
          case T_FLOAT: {
            writeNewArray(frame, "[F", 1);
            break;
          }
          case T_DOUBLE: {
            writeNewArray(frame, "[D", 1);
            break;
          }
          case T_BYTE: {
            writeNewArray(frame, "[B", 1);
            break;
          }
          case T_SHORT: {
            writeNewArray(frame, "[S", 1);
            break;
          }
          case T_INT: {
            writeNewArray(frame, "[I", 1);
            break;
          }
          case T_LONG: {
            writeNewArray(frame, "[J", 1);
            break;
          }
          default: {
            throw new JillException("Unsupported array type.");
          }
        }
        break;
      }
      default: {
        throw new JillException("Not yet supported " + Printer.OPCODES[intInsn.getOpcode()]);
      }
    }

    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull MultiANewArrayInsnNode manaIns) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();
    writeStackAccess(nextFrame, TOP_OF_STACK);
    writeNewArray(frame, manaIns.desc, manaIns.dims);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeNewArray(
      @Nonnull Frame<BasicValue> frame, @Nonnull String typeDesc, @Nonnegative int dims)
      throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.NEW_ARRAY);
    writer.writeOpen();
    writer.writeId(typeDesc);
    writer.writeOpenNodeList();
    for (int i = (dims - 1); i >= 0; i--) {
      writeStackAccess(frame, TOP_OF_STACK - i);
    }
    writer.writeCloseNodeList();
    writer.writeOpenNodeList(); // Empty initializers list.
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeArrayRef(@Nonnull Frame<BasicValue> frame, int startIdx,
      @Nonnegative int opcode) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ARRAY_REF);
    writer.writeOpen();
    Type refType = frame.getStack(frame.getStackSize() + startIdx).getType();

    // Ensure reference to array, or null. Null case can happen in this case:
    // int a[] = null;
    // return a[0] <- aload_0, iconst_0, iaload
    assert refType.getSort() == Type.ARRAY || "null".equals(refType.getInternalName());

    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.REINTERPRETCAST_OPERATION);
    writer.writeOpen();
    switch (opcode) {
      case BALOAD:
      case BASTORE: {
        if (refType.getDescriptor().equals("[Z")) {
          writer.writeId(Type.getType("[Z").getDescriptor());
        } else {
          writer.writeId(Type.getType("[B").getDescriptor());
        }
        break;
      }
      case CALOAD:
      case CASTORE: {
        writer.writeId(Type.getType("[C").getDescriptor());
        break;
      }
      case SALOAD:
      case SASTORE: {
        writer.writeId(Type.getType("[S").getDescriptor());
        break;
      }
      case IALOAD:
      case IASTORE: {
        writer.writeId(Type.getType("[I").getDescriptor());
        break;
      }
      case LALOAD:
      case LASTORE: {
        writer.writeId(Type.getType("[J").getDescriptor());
        break;
      }
      case FALOAD:
      case FASTORE: {
        writer.writeId(Type.getType("[F").getDescriptor());
        break;
      }
      case DALOAD:
      case DASTORE: {
        writer.writeId(Type.getType("[D").getDescriptor());
        break;
      }
      case AALOAD:
      case AASTORE: {
        writer.writeId(Type.getType("[Ljava/lang/Object;").getDescriptor());
        break;
      }
      default: {
        throw new JillException("Not yet supported " + Printer.OPCODES[opcode]);
      }
    }
    writeStackAccess(frame, startIdx);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();

    writeStackAccess(frame, startIdx + 1);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull LookupSwitchInsnNode switchInsn, @Nonnegative int idx) throws IOException {
    List<String> cases = new ArrayList<String>();
    List<Case> casesLabelNodeAndKey = new ArrayList<Case>();
    Case defaultCase = new Case(switchInsn.dflt, idx, null);
    casesLabelNodeAndKey.add(defaultCase);
    cases.add(defaultCase.caseId);
    int caseIdx = 0;
    for (LabelNode labelNode : switchInsn.labels) {
      Case c = new Case(labelNode, idx, switchInsn.keys.get(caseIdx++));
      casesLabelNodeAndKey.add(c);
      cases.add(c.caseId);
    }
    writeSwitch(frame, cases, casesLabelNodeAndKey);
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull TableSwitchInsnNode switchInsn, @Nonnegative int idx) throws IOException {
    List<String> cases = new ArrayList<String>();
    List<Case> casesLabelNodeAndKey = new ArrayList<Case>();
    Case defaultCase = new Case(switchInsn.dflt, idx, null);
    casesLabelNodeAndKey.add(defaultCase);
    cases.add(defaultCase.caseId);
    int key = switchInsn.min;
    for (LabelNode labelNode : switchInsn.labels) {
      Case c = new Case(labelNode, idx, Integer.valueOf(key++));
      casesLabelNodeAndKey.add(c);
      cases.add(c.caseId);
    }
    writeSwitch(frame, cases, casesLabelNodeAndKey);
  }

  private void writeSwitch(@Nonnull Frame<BasicValue> frame, @Nonnull List<String> cases,
      @Nonnull List<Case> casesLabelNodeAndKey)
      throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.SWITCH_STATEMENT);
    writer.writeOpen();
    writeStackAccess(frame, TOP_OF_STACK);
    writer.writeIds(cases);
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.BLOCK);
    writer.writeOpen();
    writer.writeOpenNodeList();
    for (Case c : casesLabelNodeAndKey) {
      sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
      writer.writeCatchBlockIds(currentCatchList);
      writer.writeKeyword(Token.CASE_STATEMENT);
      writer.writeOpen();
      writer.writeId(c.caseId);
      writeValue(c.key);
      sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
      writer.writeClose();
      writeGoto(c.labelNode);
    }
    writer.writeCloseNodeList();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull TypeInsnNode typeInsn) throws IOException {

    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();
    writeStackAccess(nextFrame, TOP_OF_STACK);

    String descriptor = Type.getObjectType(typeInsn.desc).getDescriptor();

    switch (typeInsn.getOpcode()) {
      case NEW: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ALLOC);
        writer.writeOpen();
        writer.writeId(descriptor);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case ANEWARRAY: {
        writeNewArray(frame, "[" + descriptor, 1);
        break;
      }
      case INSTANCEOF: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.REINTERPRETCAST_OPERATION);
        writer.writeOpen();
        writer.writeId(Type.BOOLEAN_TYPE.getDescriptor());
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.INSTANCE_OF);
        writer.writeOpen();
        writeStackAccess(frame, TOP_OF_STACK);
        writer.writeId(descriptor);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass,
            currentLine + 1);
        writer.writeClose();
        break;
      }
      case CHECKCAST: {
        writeCastOperation(Token.DYNAMIC_CAST_OPERATION, frame, descriptor, TOP_OF_STACK);
        break;
      }
      default: {
        throw new JillException("Not yet supported " + Printer.OPCODES[typeInsn.getOpcode()]);
      }
    }

    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull FieldInsnNode fldInsn) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();

    switch (fldInsn.getOpcode()) {
      case PUTFIELD: {
        writeInstanceFieldRef(fldInsn, frame, TOP_OF_STACK - 1);
        if (Type.getType(fldInsn.desc) == Type.BOOLEAN_TYPE) {
          writeCastOperation(Token.REINTERPRETCAST_OPERATION, frame,
              Type.BOOLEAN_TYPE.getDescriptor(), TOP_OF_STACK);
        } else {
          writeStackAccess(frame, TOP_OF_STACK);
        }
        break;
      }
      case PUTSTATIC: {
        writeStaticFieldRef(fldInsn);
        if (Type.getType(fldInsn.desc) == Type.BOOLEAN_TYPE) {
          writeCastOperation(Token.REINTERPRETCAST_OPERATION, frame,
              Type.BOOLEAN_TYPE.getDescriptor(), TOP_OF_STACK);
        } else {
          writeStackAccess(frame, TOP_OF_STACK);
        }
        break;
      }
      case GETFIELD: {
        writeStackAccess(nextFrame, TOP_OF_STACK);
        if (Type.getType(fldInsn.desc) == Type.BOOLEAN_TYPE) {
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeKeyword(Token.REINTERPRETCAST_OPERATION);
          writer.writeOpen();
          writer.writeId(Type.INT_TYPE.getDescriptor());
          writeInstanceFieldRef(fldInsn, frame, TOP_OF_STACK);
          sourceInfoWriter.writeDebugEnd(currentClass,
              currentLine + 1);
          writer.writeClose();
        } else {
          writeInstanceFieldRef(fldInsn, frame, TOP_OF_STACK);
        }
        break;
      }
      case GETSTATIC: {
        writeStackAccess(nextFrame, TOP_OF_STACK);
        if (Type.getType(fldInsn.desc) == Type.BOOLEAN_TYPE) {
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeKeyword(Token.REINTERPRETCAST_OPERATION);
          writer.writeOpen();
          writer.writeId(Type.INT_TYPE.getDescriptor());
          writeStaticFieldRef(fldInsn);
          sourceInfoWriter.writeDebugEnd(currentClass,
              currentLine + 1);
          writer.writeClose();
        } else {
          writeStaticFieldRef(fldInsn);
        }
        break;
      }
      default:
        throw new JillException("Not yet supported " + Printer.OPCODES[fldInsn.getOpcode()]);
    }

    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();

  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull MethodInsnNode mthInsn) throws IOException {
    switch (mthInsn.getOpcode()) {
      case INVOKEINTERFACE:
      case INVOKESTATIC:
      case INVOKEVIRTUAL:
      case INVOKESPECIAL: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();

        Type returnType = Type.getReturnType(mthInsn.desc);
        if (returnType != Type.VOID_TYPE) {
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeKeyword(Token.ASG_OPERATION);
          writer.writeOpen();
          writeStackAccess(nextFrame, TOP_OF_STACK);
          if (returnType == Type.BOOLEAN_TYPE) {
            sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
            writer.writeKeyword(Token.REINTERPRETCAST_OPERATION);
            writer.writeOpen();
            writer.writeId(Type.INT_TYPE.getDescriptor());
          }
        }

        DispatchKind dispatchKind;
        MethodKind methodKind;
        MethodCallReceiverKind receiverKind;
        switch (mthInsn.getOpcode()) {
          case INVOKEINTERFACE: {
            dispatchKind = DispatchKind.VIRTUAL;
            methodKind = MethodKind.INSTANCE_VIRTUAL;
            receiverKind = MethodCallReceiverKind.INTERFACE;
            break;
          }
          case INVOKESTATIC: {
            dispatchKind = DispatchKind.DIRECT;
            methodKind = MethodKind.STATIC;
            receiverKind = MethodCallReceiverKind.CLASS;
            break;
          }
          case INVOKEVIRTUAL: {
            dispatchKind = DispatchKind.VIRTUAL;
            methodKind = MethodKind.INSTANCE_VIRTUAL;
            receiverKind = MethodCallReceiverKind.CLASS;
            break;
          }
          case INVOKESPECIAL: {
            if (mthInsn.owner.equals(currentClass.name) || mthInsn.name.equals("<init>")) {
              dispatchKind = DispatchKind.DIRECT;
              methodKind = MethodKind.INSTANCE_NON_VIRTUAL;
              receiverKind = MethodCallReceiverKind.CLASS;
            } else {
              dispatchKind = DispatchKind.DIRECT;
              methodKind = MethodKind.INSTANCE_VIRTUAL;
              receiverKind = MethodCallReceiverKind.CLASS;
            }
            break;
          }
          default: {
            throw new JillException("Opcode not supported " + Printer.OPCODES[mthInsn.getOpcode()]);
          }
        }

        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.METHOD_CALL);
        writer.writeOpen();
        Type receiverType = Type.getObjectType(mthInsn.owner);
        int stackArgIndex = Type.getArgumentTypes(mthInsn.desc).length;

        if (mthInsn.getOpcode() == INVOKESTATIC) {
          writer.writeNull(); // Instance
        } else {
          // Add implicit argument 'this'
          stackArgIndex++;
          // Cast instance to receiver type
          if (receiverType.equals(frame.getStack(frame.getStackSize() - stackArgIndex).getType())
              || mthInsn.name.equals("<init>")) {
            // It is not possible to add cast on object before call to init
            writeStackAccess(frame, -stackArgIndex);
          } else {
            writeCastOperation(Token.REINTERPRETCAST_OPERATION, frame, receiverType.getDescriptor(),
                -stackArgIndex);
          }
          stackArgIndex--;
        }

        if (receiverType.getSort() == Type.ARRAY) {
          // Currently Jack file does not support that array types are used as a receiver or
          // declaring type into a method call.
          receiverType = Type.getType(Object.class);
        }
        writer.writeId(receiverType.getDescriptor()); // Receiver type
        writer.writeReceiverKindEnum(receiverKind);

        writer.writeId(mthInsn.name);
        Type[] argumentTypes = Type.getArgumentTypes(mthInsn.desc);
        List<String> argsTypeIds = new ArrayList<String>(argumentTypes.length);
        for (Type argType : argumentTypes) {
          argsTypeIds.add(argType.getDescriptor());
        }
        writer.writeIds(argsTypeIds);
        writer.writeMethodKindEnum(methodKind);

        writer.writeId(returnType.getDescriptor());
        int argIdx = 0;
        writer.writeOpenNodeList();
        while (stackArgIndex > 0) {
          Type argType = argumentTypes[argIdx++];
          if (argType.getSort() == Type.OBJECT || argType.getSort() == Type.ARRAY
              || argType.getSort() == Type.BYTE || argType.getSort() == Type.CHAR
              || argType.getSort() == Type.SHORT || argType.getSort() == Type.BOOLEAN) {
            writeCastOperation(Token.REINTERPRETCAST_OPERATION, frame, argType.getDescriptor(),
                -stackArgIndex);
          } else {
            writeStackAccess(frame, -stackArgIndex);
          }
          stackArgIndex--;
        }
        writer.writeCloseNodeList();
        writer.writeDispatchKindEnum(dispatchKind);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();

        if (returnType != Type.VOID_TYPE) {
          if (returnType == Type.BOOLEAN_TYPE) {
            sourceInfoWriter.writeDebugEnd(currentClass,
                currentLine + 1);
            writer.writeClose();
          }
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();
        }

        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      default: {
        throw new JillException("Not yet supported " + Printer.OPCODES[mthInsn.getOpcode()]);
      }
    }
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame,
      @Nonnull VarInsnNode varInsn) throws IOException {
    switch (varInsn.getOpcode()) {
      case FLOAD:
      case DLOAD:
      case LLOAD:
      case ILOAD:
      case ALOAD: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        if (getLocalVariable(frame, varInsn.var).getType() == Type.BOOLEAN_TYPE) {
          writeCastOperation(Token.REINTERPRETCAST_OPERATION, getLocalVariable(frame, varInsn.var),
              Type.INT_TYPE.getDescriptor());
        } else {
          writeLocalAccess(frame, varInsn.var);
        }
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case FSTORE:
      case DSTORE:
      case LSTORE:
      case ISTORE:
      case ASTORE: {
        // Uninitialize variable means dead store. Do not generate them.
        if (nextFrame.getLocal(varInsn.var) != BasicValue.UNINITIALIZED_VALUE) {
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeCatchBlockIds(currentCatchList);
          writer.writeKeyword(Token.EXPRESSION_STATEMENT);
          writer.writeOpen();
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeKeyword(Token.ASG_OPERATION);
          writer.writeOpen();
          writeLocalAccess(nextFrame, varInsn.var);
          Type destType = getLocalVariable(nextFrame, varInsn.var).getType();
          if (destType == Type.BOOLEAN_TYPE) {
            writeCastOperation(Token.REINTERPRETCAST_OPERATION, frame,
                Type.BOOLEAN_TYPE.getDescriptor(), TOP_OF_STACK);
          } else if (getStackVariable(frame, TOP_OF_STACK).getType() != destType) {
            writeCastOperation(Token.REINTERPRETCAST_OPERATION, frame, destType.getDescriptor(),
                TOP_OF_STACK);
          } else {
            writeStackAccess(frame, TOP_OF_STACK);
          }
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();
        }
        break;
      }
      default: {
        throw new JillException("Not yet supported " + Printer.OPCODES[varInsn.getOpcode()]);
      }
    }
  }

  private void writeInsn(@Nonnull Frame<BasicValue> frame,
      @CheckForNull Frame<BasicValue> nextFrame, @Nonnull InsnNode insn) throws IOException {
    switch (insn.getOpcode()) {
      case ICONST_M1:
      case ICONST_0:
      case ICONST_1:
      case ICONST_2:
      case ICONST_3:
      case ICONST_4:
      case ICONST_5: {
        assert nextFrame != null;
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        writeValue(insn.getOpcode() - ICONST_0);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case ACONST_NULL: {
        assert nextFrame != null;
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        writeValue();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case LCONST_0:
      case LCONST_1: {
        assert nextFrame != null;
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        writeValue((long) (insn.getOpcode() - LCONST_0));
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case FCONST_0:
      case FCONST_1:
      case FCONST_2: {
        assert nextFrame != null;
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        writeValue((float) (insn.getOpcode() - FCONST_0));
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case DCONST_0:
      case DCONST_1: {
        assert nextFrame != null;
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        writeValue((double) (insn.getOpcode() - DCONST_0));
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case D2L:
      case F2L:
      case I2L: {
        assert nextFrame != null;
        writePrimitiveTypeConversion(long.class, frame, nextFrame);
        break;
      }
      case D2F:
      case I2F:
      case L2F:{
        assert nextFrame != null;
        writePrimitiveTypeConversion(float.class, frame, nextFrame);
        break;
      }
      case F2D:
      case I2D:
      case L2D: {
        assert nextFrame != null;
        writePrimitiveTypeConversion(double.class, frame, nextFrame);
        break;
      }
      case D2I:
      case F2I:
      case L2I: {
        assert nextFrame != null;
        writePrimitiveTypeConversion(int.class, frame, nextFrame);
        break;
      }
      case I2B: {
        assert nextFrame != null;
        writePrimitiveTypeConversion(byte.class, frame, nextFrame);
        break;
      }
      case I2C: {
        assert nextFrame != null;
        writePrimitiveTypeConversion(char.class, frame, nextFrame);
        break;
      }
      case I2S: {
        assert nextFrame != null;
        writePrimitiveTypeConversion(short.class, frame, nextFrame);
        break;
      }
      case DRETURN:
      case LRETURN:
      case FRETURN: {
        writeReturn(frame, TOP_OF_STACK);
        break;
      }
      case ARETURN:
      case IRETURN: {
        Type returnType = Type.getReturnType(currentMethod.desc);
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.RETURN_STATEMENT);
        writer.writeOpen();
        writeCastOperation(Token.REINTERPRETCAST_OPERATION, frame, returnType.getDescriptor(),
            TOP_OF_STACK);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case RETURN: {
        writeReturn(frame, 0);
        break;
      }
      case DADD:
      case FADD:
      case IADD:
      case LADD: {
        assert nextFrame != null;
        writeBinaryOperation(Token.ADD_OPERATION, frame, nextFrame);
        break;
      }
      case LCMP:
      case FCMPL:
      case FCMPG:
      case DCMPL:
      case DCMPG: {
        assert nextFrame != null;
        Variable lhs = getStackVariable(frame, TOP_OF_STACK - 1);
        Variable rhs = getStackVariable(frame, TOP_OF_STACK);
        Variable result = getStackVariable(nextFrame, TOP_OF_STACK);
        cmpOperands.put(result, new CmpOperands(insn.getOpcode(), lhs, rhs));
        break;
      }
      case DSUB:
      case FSUB:
      case ISUB:
      case LSUB: {
        assert nextFrame != null;
        writeBinaryOperation(Token.SUB_OPERATION, frame, nextFrame);
        break;
      }
      case DMUL:
      case FMUL:
      case IMUL:
      case LMUL: {
        assert nextFrame != null;
        writeBinaryOperation(Token.MUL_OPERATION, frame, nextFrame);
        break;
      }
      case DDIV:
      case FDIV:
      case IDIV:
      case LDIV: {
        assert nextFrame != null;
        writeBinaryOperation(Token.DIV_OPERATION, frame, nextFrame);
        break;
      }
      case DREM:
      case FREM:
      case IREM:
      case LREM: {
        assert nextFrame != null;
        writeBinaryOperation(Token.MOD_OPERATION, frame, nextFrame);
        break;
      }
      case DNEG:
      case FNEG:
      case INEG:
      case LNEG: {
        assert nextFrame != null;
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.PREFIX_NEG_OPERATION);
        writer.writeOpen();
        writeStackAccess(frame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case ISHL:
      case LSHL: {
        assert nextFrame != null;
        writeBinaryOperation(Token.SHL_OPERATION, frame, nextFrame);
        break;
      }
      case ISHR:
      case LSHR: {
        assert nextFrame != null;
        writeBinaryOperation(Token.SHR_OPERATION, frame, nextFrame);
        break;
      }
      case IUSHR:
      case LUSHR: {
        assert nextFrame != null;
        writeBinaryOperation(Token.SHRU_OPERATION, frame, nextFrame);
        break;
      }
      case IAND:
      case LAND: {
        assert nextFrame != null;
        writeBinaryOperation(Token.BIT_AND_OPERATION, frame, nextFrame);
        break;
      }
      case IOR:
      case LOR: {
        assert nextFrame != null;
        writeBinaryOperation(Token.BIT_OR_OPERATION, frame, nextFrame);
        break;
      }
      case IXOR:
      case LXOR: {
        assert nextFrame != null;
        writeBinaryOperation(Token.BIT_XOR_OPERATION, frame, nextFrame);
        break;
      }
      case ARRAYLENGTH: {
        assert nextFrame != null;
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ARRAY_LENGTH);
        writer.writeOpen();
        writeStackAccess(frame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }

      case IALOAD:
      case LALOAD:
      case FALOAD:
      case DALOAD:
      case AALOAD:
      case BALOAD:
      case CALOAD:
      case SALOAD: {
        assert nextFrame != null;
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(nextFrame, TOP_OF_STACK);
        writeArrayRef(frame, TOP_OF_STACK - 1, insn.getOpcode());
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case IASTORE:
      case LASTORE:
      case FASTORE:
      case DASTORE:
      case AASTORE:
      case BASTORE:
      case CASTORE:
      case SASTORE: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeArrayRef(frame, TOP_OF_STACK - 2, insn.getOpcode());
        writeStackAccess(frame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case MONITORENTER: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.LOCK);
        writer.writeOpen();
        writeStackAccess(frame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case MONITOREXIT: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.UNLOCK);
        writer.writeOpen();
        writeStackAccess(frame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case SWAP: {
        // frame and nextFrame have the same height, thus frame can always be used to compute stack
        // variables.
        Variable tmpVar = getTempVarFromTopOfStack(frame);

        // tmpVar = frame.stack[frame.stack.size() + TOP_OF_STACK - 1]
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeLocalRef(tmpVar);
        writeStackAccess(frame, TOP_OF_STACK - 1);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();

        // frame.stack[frame.stack.size() + TOP_OF_STACK - 1] =
        // frame.stack[frame.stack.size() + TOP_OF_STACK]
        writeAssign(frame, TOP_OF_STACK, frame, TOP_OF_STACK - 1);

        // frame.stack[frame.stack.size() + TOP_OF_STACK] = tmpVar
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.EXPRESSION_STATEMENT);
        writer.writeOpen();
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(Token.ASG_OPERATION);
        writer.writeOpen();
        writeStackAccess(frame, TOP_OF_STACK);
        writeLocalRef(tmpVar);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case DUP: {
        assert nextFrame != null;
        writeDup(frame, nextFrame);
        break;
      }
      case DUP2: {
        assert nextFrame != null;
        if (frame.getStack(frame.getStackSize() + TOP_OF_STACK).getSize() == 1) {
          assert frame.getStack(frame.getStackSize() + TOP_OF_STACK - 1).getSize() == 1;
          writeDup2(frame, nextFrame);
        } else {
          writeDup(frame, nextFrame);
        }
        break;
      }
      case DUP_X1: {
        assert nextFrame != null;
        assert frame.getStack(frame.getStackSize() + TOP_OF_STACK).getSize() == 1;
        assert frame.getStack(frame.getStackSize() + TOP_OF_STACK - 1).getSize() == 1;
        writeDupX1(frame, nextFrame);
        break;
      }
      case DUP_X2: {
        assert nextFrame != null;
        Variable value1 = getStackVariable(frame, TOP_OF_STACK);
        Variable value2 = getStackVariable(frame, TOP_OF_STACK - 1);
        assert value1.getType().getSize() == 1;
        if (value2.getType().getSize() == 1) {
          Variable value3 = getStackVariable(frame, TOP_OF_STACK - 2);
          assert value3.getType().getSize() == 1;
          writeDupX2(frame, nextFrame);
        } else {
          writeDupX1(frame, nextFrame);
        }
        break;
      }
      case DUP2_X1: {
        assert nextFrame != null;
        Variable value1 = getStackVariable(frame, TOP_OF_STACK);
        Variable value2 = getStackVariable(frame, TOP_OF_STACK - 1);
        assert value2.getType().getSize() == 1;
        if (value1.getType().getSize() == 1) {
          Variable value3 = getStackVariable(frame, TOP_OF_STACK - 2);
          assert value3.getType().getSize() == 1;
          writeDup2X1(frame, nextFrame);
        } else {
          writeDupX1(frame, nextFrame);
        }
        break;
      }
      case DUP2_X2: {
        assert nextFrame != null;
        Variable value1 = getStackVariable(frame, TOP_OF_STACK);
        Variable value2 = getStackVariable(frame, TOP_OF_STACK - 1);
        if (value1.getType().getSize() == 1) {
          Variable value3 = getStackVariable(frame, TOP_OF_STACK - 2);
          if (value3.getType().getSize() == 1) {
            Variable value4 = getStackVariable(frame, TOP_OF_STACK - 3);
            assert value4.getType().getSize() == 1;
            writeDup2X2(frame, nextFrame);
          } else {
            writeDup2X1(frame, nextFrame);
          }
        } else {
          if (value2.getType().getSize() == 1) {
            Variable value3 = getStackVariable(frame, TOP_OF_STACK - 2);
            assert value3.getType().getSize() == 1;
            writeDupX2(frame, nextFrame);
          } else  {
            writeDupX1(frame, nextFrame);
          }
        }
        break;
      }
      case NOP:
      case POP:
      case POP2:{
        break;
      }
      case ATHROW: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.THROW_STATEMENT);
        writer.writeOpen();
        writeStackAccess(frame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      default: {
        throw new JillException("Not yet supported " + Printer.OPCODES[insn.getOpcode()]);
      }
    }
  }

  private void writeInsn(@Nonnull Frame<BasicValue> nextFrame, @Nonnull LdcInsnNode ldcInsn)
      throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();
    writeStackAccess(nextFrame, TOP_OF_STACK);
    writeValue(ldcInsn.cst);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeInsn(
      @Nonnull Frame<BasicValue> frame, @Nonnull JumpInsnNode jumpInsn, @Nonnegative int insIndex)
      throws IOException {
    switch (jumpInsn.getOpcode()) {
      case IFNONNULL:
      case IFNULL:
      case IFEQ:
      case IFGE:
      case IFGT:
      case IFLE:
      case IFLT:
      case IFNE: {
        Variable topOfStackVariable = getStackVariable(frame, TOP_OF_STACK);
        CmpOperands cmpOps = cmpOperands.get(topOfStackVariable);
        if (cmpOps != null) {
          // CmpOperands concerns double, float and long types
          assert jumpInsn.getOpcode() != IFNONNULL && jumpInsn.getOpcode() != IFNULL;
          // Not operator can be generate only for double and long types to manage comparisons with
          // Nan.
          Token comparisonToken = getConditionToken(jumpInsn.getOpcode());
          boolean needNotoperator = needNotOperator(comparisonToken, cmpOps);

          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeCatchBlockIds(currentCatchList);
          writer.writeKeyword(Token.IF_STATEMENT);
          writer.writeOpen();
          if (needNotoperator) {
            sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
            writer.writeKeyword(Token.PREFIX_NOT_OPERATION);
            writer.writeOpen();
          } else {
            // Condition is inverted to be compliant with language level semantics
            // This has been done for comparison to NaN, which forces the branching order.
            comparisonToken = invertComparisonToken(comparisonToken);
          }

          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeKeyword(comparisonToken);
          writer.writeOpen();
          writeLocalRef(cmpOps.lhs);
          writeLocalRef(cmpOps.rhs);
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();

          if (needNotoperator) {
            sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
            writer.writeClose();
          }

          int labeledStatmentIndex = insIndex + 1;
          writeGoto(labeledStatmentIndex);
          writeGoto(jumpInsn.label);
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();

          insertLabeledStatementIfNecessary(labeledStatmentIndex);

          cmpOperands.remove(topOfStackVariable);
        } else {
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeCatchBlockIds(currentCatchList);
          writer.writeKeyword(Token.IF_STATEMENT);
          writer.writeOpen();
          Token conditionalToken = getConditionToken(jumpInsn.getOpcode());
          sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
          writer.writeKeyword(conditionalToken);
          writer.writeOpen();
          writeStackAccess(frame, TOP_OF_STACK);
          Variable v = getStackVariable(frame, TOP_OF_STACK);
          if (v.getType().equals(Type.BOOLEAN_TYPE)) {
            writeValue(false);
          } else if (v.getType().equals(Type.BYTE_TYPE)
               || v.getType().equals(Type.CHAR_TYPE)
               || v.getType().equals(Type.SHORT_TYPE)
               || v.getType().equals(Type.INT_TYPE)) {
            writeValue(0);
          } else {
            writeValue();
          }
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();
          writeGoto(jumpInsn.label);
          writer.writeNull();
          sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
          writer.writeClose();
        }
        break;
      }
      case IF_ICMPEQ:
      case IF_ICMPGE:
      case IF_ICMPGT:
      case IF_ICMPLE:
      case IF_ICMPLT:
      case IF_ICMPNE:
      case IF_ACMPEQ:
      case IF_ACMPNE: {
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeCatchBlockIds(currentCatchList);
        writer.writeKeyword(Token.IF_STATEMENT);
        writer.writeOpen();
        Token conditionalToken = getConditionToken(jumpInsn.getOpcode());
        sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
        writer.writeKeyword(conditionalToken);
        writer.writeOpen();
        writeStackAccess(frame, TOP_OF_STACK - 1);
        writeStackAccess(frame, TOP_OF_STACK);
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        writeGoto(jumpInsn.label);
        writer.writeNull();
        sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
        writer.writeClose();
        break;
      }
      case GOTO: {
        writeGoto(jumpInsn.label);
        break;
      }
      default: {
        throw new JillException("Not yet supported " + Printer.OPCODES[jumpInsn.getOpcode()]);
      }
    }
  }


  private void insertLabeledStatementIfNecessary(@Nonnegative int labeledStatmentIndex)
      throws IOException {
    AbstractInsnNode existing = currentMethod.instructions.get(labeledStatmentIndex);
    if (existing instanceof LabelNode) {
      return;
    } else {
      writeLabelInsn(labeledStatmentIndex);
    }
  }

  @Nonnull
  private Token getConditionToken(@Nonnegative int opcode) {
    switch (opcode) {
      case IFNULL:
      case IF_ACMPEQ:
      case IF_ICMPEQ:
      case IFEQ:
        return Token.EQ_OPERATION;
      case IF_ICMPGE:
      case IFGE:
        return Token.GTE_OPERATION;
      case IF_ICMPGT:
      case IFGT:
        return Token.GT_OPERATION;
      case IF_ICMPLE:
      case IFLE:
        return Token.LTE_OPERATION;
      case IF_ICMPLT:
      case IFLT:
        return Token.LT_OPERATION;
      case IFNONNULL:
      case IF_ACMPNE:
      case IF_ICMPNE:
      case IFNE:
        return Token.NEQ_OPERATION;
    }
    throw new JillException("Unsupported condition.");
  }

  @Nonnull
  private Token invertComparisonToken(@Nonnull Token cmpToken) {
    switch (cmpToken) {
      case GTE_OPERATION: {
        return Token.LT_OPERATION;
      }
      case GT_OPERATION: {
        return Token.LTE_OPERATION;
      }
      case LTE_OPERATION: {
        return Token.GT_OPERATION;
      }
      case LT_OPERATION: {
        return Token.GTE_OPERATION;
      }
      case EQ_OPERATION: {
        return Token.NEQ_OPERATION;
      }
      case NEQ_OPERATION: {
        return Token.EQ_OPERATION;
      }
      default: {
        return cmpToken;
      }
    }
  }

  @Nonnull
  private boolean needNotOperator(@Nonnull Token cmpToken, @Nonnull CmpOperands cmpOps) {
    switch (cmpToken) {
      case GTE_OPERATION:
      case GT_OPERATION: {
        return !isCmpg(cmpOps);
      }
      case LTE_OPERATION:
      case LT_OPERATION: {
        return !isCmpl(cmpOps);
      }
      default: {
        return false;
      }
    }
  }

  private boolean isCmpl(@Nonnull CmpOperands cmpOps) {
    return cmpOps.opcode == DCMPL || cmpOps.opcode == FCMPL;
  }

  private boolean isCmpg(@Nonnull CmpOperands cmpOps) {
    return cmpOps.opcode == DCMPG || cmpOps.opcode == FCMPG;
  }

  private void writeGoto(LabelNode labelNode) throws IOException {
    int insIndex = currentMethod.instructions.indexOf(labelNode);
    writeGoto(insIndex);
  }

  private void writeGoto(int insIndex) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.GOTO);
    writer.writeOpen();
    writer.writeId(Integer.toString(insIndex));
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeReturn(@Nonnull Frame<BasicValue> frame, int stackIdx) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.RETURN_STATEMENT);
    writer.writeOpen();
    if (stackIdx == 0) {
      writer.writeNull();
    } else {
      writeStackAccess(frame, stackIdx);
    }
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeStackAccess(@Nonnull Frame<BasicValue> frame, int stackIdx)
      throws IndexOutOfBoundsException, IOException {
    writeLocalRef(getStackVariable(frame, stackIdx));
  }

  private void writeLocalAccess(@Nonnull Frame<BasicValue> frame, @Nonnegative int localIdx)
      throws IndexOutOfBoundsException, IOException {
    writeLocalRef(getLocalVariable(frame, localIdx));
  }

  private void writeLocalRef(@Nonnull Variable v) throws IOException {
    if (v.isThis()) {
      sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
      writer.writeKeyword(Token.THIS_REF);
      writer.writeOpen();
      writer.writeId(v.getType().getDescriptor());
      sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
      writer.writeClose();
    } else {
      Token token = v.isParameter() ? Token.PARAMETER_REF : Token.LOCAL_REF;
      sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
      writer.writeKeyword(token);
      writer.writeOpen();
      writer.writeId(v.getId());
      sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
      writer.writeClose();
    }
  }

  private void writeInstanceFieldRef(@Nonnull FieldInsnNode fldInsn,
      @Nonnull Frame<BasicValue> frame, int offset) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.FIELD_REF);
    writer.writeOpen();
    writer.writeId(fldInsn.name);
    writer.writeId(fldInsn.desc);
    writer.writeId(Type.getObjectType(fldInsn.owner).getDescriptor());
    writer.writeFieldRefKindEnum(FieldRefKind.INSTANCE);
    writeStackAccess(frame, offset);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeStaticFieldRef(@Nonnull FieldInsnNode fldInsn)
      throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.FIELD_REF);
    writer.writeOpen();
    writer.writeId(fldInsn.name);
    writer.writeId(fldInsn.desc);
    writer.writeId(Type.getObjectType(fldInsn.owner).getDescriptor());
    writer.writeFieldRefKindEnum(FieldRefKind.STATIC);
    writer.writeNull();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  public void dump() {
    Textifier t = new Textifier();
    Frame<BasicValue>[] frames = analyzer.getFrames();
    List<Object> text = t.getText();
    int insnIdx = 0;

    currentMethod.accept(new TraceMethodVisitor(t));

    for (Object o : text) {
      if (insnIdx < frames.length && frames[insnIdx] != null) {
        System.out.print(insnIdx + " : [");
        for (int i = 0; i < frames[insnIdx].getLocals(); i++) {
          BasicValue bv = frames[insnIdx].getLocal(i);
          System.out.print(bv.toString() + " ");
        }
        System.out.print("| ");
        for (int i = 0; i < frames[insnIdx].getStackSize(); i++) {
          BasicValue bv = frames[insnIdx].getStack(i);
          System.out.print(bv.toString() + " ");
        }
        System.out.println("]");
      }
      System.out.print(o);
      insnIdx++;
    }
  }

  private void writeLocals() throws IOException {
    writer.writeOpenNodeList();

    if (currentMethod.instructions.size() != 0) {
      Iterator<Variable> varIt = collectLocals();
      while (varIt.hasNext()) {
        writeLocal(varIt.next());
      }
    }

    writer.writeCloseNodeList();
  }

  private void writeLocal(Variable v) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.LOCAL);
    writer.writeOpen();
    writer.writeId(v.getId());
    writer.writeInt(v.isSynthetic() ? Opcodes.ACC_SYNTHETIC : NO_MODIFIER);
    writer.writeId(v.getType().getDescriptor());
    writer.writeId(v.getName());
    writer.writeOpenNodeList(); // Empty annotation set, annotations on locals are not kept
    writer.writeCloseNodeList();
    writer.writeOpenNodeList();
    if (v.hasSignature()) {
      writer.writeKeyword(Token.GENERIC_SIGNATURE); // Marker generic signature
      writer.writeOpen();
      writer.writeString(v.getSignature());
      writer.writeClose();
    }
    writer.writeCloseNodeList();
    // TODO(mikaelpeltier): Add debug information.
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  private void writePrimitiveTypeConversion(@Nonnull Class<?> targetType,
      @Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame) throws IOException {
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();
    writeStackAccess(nextFrame, TOP_OF_STACK);
    writeCastOperation(Token.DYNAMIC_CAST_OPERATION, frame, Type.getDescriptor(targetType),
        TOP_OF_STACK);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeCastOperation(@Nonnull Token cast, @Nonnull Variable var,
      @Nonnull String typeDesc) throws IOException {
    assert cast == Token.DYNAMIC_CAST_OPERATION || cast == Token.REINTERPRETCAST_OPERATION;
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(cast);
    writer.writeOpen();
    writer.writeId(typeDesc);
    writeLocalRef(var);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeCastOperation(@Nonnull Token cast, @Nonnull Frame<BasicValue> frame,
      @Nonnull String typeDesc, int stackIdx) throws IOException {
    writeCastOperation(cast, getStackVariable(frame, stackIdx), typeDesc);
  }

  private void writeDup(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame)
      throws IOException {
    writeAssign(frame, TOP_OF_STACK, nextFrame, TOP_OF_STACK);
  }

  private void writeDupX1(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame)
      throws IOException {
    writeAssign(frame, TOP_OF_STACK, nextFrame, TOP_OF_STACK);
    writeAssign(frame, TOP_OF_STACK - 1, nextFrame, TOP_OF_STACK - 1);
    writeAssign(nextFrame, TOP_OF_STACK, nextFrame, TOP_OF_STACK - 2);
  }

  private void writeDupX2(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame)
      throws IOException {
    writeAssign(frame, TOP_OF_STACK, nextFrame, TOP_OF_STACK);
    writeAssign(frame, TOP_OF_STACK - 1, nextFrame, TOP_OF_STACK - 1);
    writeAssign(frame, TOP_OF_STACK - 2, nextFrame, TOP_OF_STACK - 2);
    writeAssign(nextFrame, TOP_OF_STACK, nextFrame, TOP_OF_STACK - 3);
  }

  private void writeDup2(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame)
      throws IOException {
    writeAssign(frame, TOP_OF_STACK, nextFrame, TOP_OF_STACK);
    writeAssign(frame, TOP_OF_STACK - 1, nextFrame, TOP_OF_STACK - 1);
  }

  private void writeDup2X1(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame)
      throws IOException {
    writeAssign(frame, TOP_OF_STACK, nextFrame, TOP_OF_STACK);
    writeAssign(frame, TOP_OF_STACK - 1, nextFrame, TOP_OF_STACK - 1);
    writeAssign(frame, TOP_OF_STACK - 2, nextFrame, TOP_OF_STACK - 2);
    writeAssign(nextFrame, TOP_OF_STACK, nextFrame, TOP_OF_STACK - 3);
    writeAssign(nextFrame, TOP_OF_STACK - 1, nextFrame, TOP_OF_STACK - 4);
  }

  private void writeDup2X2(@Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame)
      throws IOException {
    writeAssign(frame, TOP_OF_STACK, nextFrame, TOP_OF_STACK);
    writeAssign(frame, TOP_OF_STACK - 1, nextFrame, TOP_OF_STACK - 1);
    writeAssign(frame, TOP_OF_STACK - 2, nextFrame, TOP_OF_STACK - 2);
    writeAssign(frame, TOP_OF_STACK - 3, nextFrame, TOP_OF_STACK - 3);
    writeAssign(nextFrame, TOP_OF_STACK, nextFrame, TOP_OF_STACK - 4);
    writeAssign(nextFrame, TOP_OF_STACK - 1, nextFrame, TOP_OF_STACK - 5);
  }

  /**
   * writes frame2.stack[frame.stack.size() + offset2] = frame1.stack[frame.stack.size() + offset1]
   *
   * @throws IOException
   */
  private void writeAssign(@Nonnull Frame<BasicValue> frame1, int offset1,
      @Nonnull Frame<BasicValue> frame2, int offset2) throws IOException {
    assert !isBooleanAssignIssue(
        getStackVariable(frame2, offset2),
        getStackVariable(frame1, offset1));
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();
    writeStackAccess(frame2, offset2);
    writeStackAccess(frame1, offset1);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  private void writeBinaryOperation(
      @Nonnull Token op, @Nonnull Frame<BasicValue> frame, @Nonnull Frame<BasicValue> nextFrame)
      throws IOException {
    assert !isBooleanAssignIssue(
        getStackVariable(frame, TOP_OF_STACK - 1),
        getStackVariable(frame, TOP_OF_STACK));
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeCatchBlockIds(currentCatchList);
    writer.writeKeyword(Token.EXPRESSION_STATEMENT);
    writer.writeOpen();
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(Token.ASG_OPERATION);
    writer.writeOpen();
    writeStackAccess(nextFrame, TOP_OF_STACK);
    sourceInfoWriter.writeDebugBegin(currentClass, currentLine);
    writer.writeKeyword(op);
    writer.writeOpen();
    writeStackAccess(frame, TOP_OF_STACK - 1);
    writeStackAccess(frame, TOP_OF_STACK);
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
    sourceInfoWriter.writeDebugEnd(currentClass, currentLine + 1);
    writer.writeClose();
  }

  @Nonnull
  private Iterator<Variable> collectLocals() {
    Set<Variable> locals = new LinkedHashSet<Variable>();

    Frame<BasicValue>[] frames = analyzer.getFrames();
    for (int frameIdx = 0; frameIdx < frames.length; frameIdx++) {
      currentPc = frameIdx;
      Frame<BasicValue> frame = frames[frameIdx];
      if (frame != null) {
        for (int localIdx = 0; localIdx < frame.getLocals(); localIdx++) {
          BasicValue bv = frame.getLocal(localIdx);
          if (bv != BasicValue.UNINITIALIZED_VALUE) {
            Variable local = getLocalVariable(frame, localIdx);
            if (!local.isParameter() && !local.isThis()) {
              locals.add(local);
            }
          }
        }
        for (int stackIdx = 0; stackIdx < frame.getStackSize(); stackIdx++) {
          Variable v = getStackVariable(frame, -stackIdx - 1);
          locals.add(v);
        }
      }
    }

    // Do not forget to collect temporary variable required by some instructions.
    for (int insnIdx = 0; insnIdx < currentMethod.instructions.size(); insnIdx++) {
      AbstractInsnNode insn = currentMethod.instructions.get(insnIdx);
      if (insn.getOpcode() == SWAP) {
        locals.add(getTempVarFromTopOfStack(frames[insnIdx]));
      }
    }

    return locals.iterator();
  }

  @Nonnull
  private Variable getTempVarFromTopOfStack(@Nonnull Frame<BasicValue> frame) {
    Variable topOfStackBeforeInst = getStackVariable(frame, TOP_OF_STACK);
    String tmpVarId = "-swap_tmp_" + typeToUntypedDesc(topOfStackBeforeInst.getType());
    Variable tmpVariable =
        getVariable(tmpVarId, tmpVarId, topOfStackBeforeInst.getType(), null);
    tmpVariable.setSynthetic();
    return tmpVariable;
  }

  private void writeParameters()
      throws IOException {
    writer.writeOpenNodeList();

    int parameterIdx = 0;
    int parameterAnnotationIdx = 0;
    currentPc = 0;

    if (!AsmHelper.isStatic(currentMethod)) {
      Type parameterType = Type.getObjectType(currentClass.name);
      LocalVariableNode lvn = getLocalVariableNode(parameterIdx);
      if (lvn == null) {
        String pid = getUnnamedParameterId(parameterIdx, parameterType);
        Variable p = getVariable(pid, pid, parameterType, null);
        p.setThis();

        Type untypedParameter = typeToUntyped(parameterType);
        String lid = getUnnamedLocalId(parameterIdx, untypedParameter);
        Variable local = getVariable(lid, lid, untypedParameter, null);

        parameter2Var.put(p, local);
      } else {
        assert parameterType.getDescriptor().equals(lvn.desc);
        Variable p = getVariable(getNamedLocalId(lvn), lvn.name, parameterType, lvn.signature);
        p.setThis();
      }
      parameterIdx++;
    }

    for (Type paramType : Type.getArgumentTypes(currentMethod.desc)) {
      LocalVariableNode lvn = getLocalVariableNode(parameterIdx);
      if (lvn == null) {
        String pid = getUnnamedParameterId(parameterIdx, paramType);
        Variable p = getVariable(pid, pid, paramType, null);
        p.setParameter();
        writeParameter(paramType, parameterIdx, p, parameterAnnotationIdx++);

        Type untypedParameter = typeToUntyped(paramType);
        String lid = getUnnamedLocalId(parameterIdx, untypedParameter);
        Variable local = getVariable(lid, lid, untypedParameter, null);

        parameter2Var.put(p, local);
      } else {
        assert paramType.getDescriptor().equals(lvn.desc);
        Variable p = getVariable(getNamedLocalId(lvn), lvn.name, paramType, lvn.signature);
        p.setParameter();
        writeParameter(paramType, parameterIdx, p, parameterAnnotationIdx++);
      }
      parameterIdx += paramType.getSize();
    }

    writer.writeCloseNodeList();
  }

  private void writeParameter(@Nonnull Type paramType, @Nonnegative int localIdx,
      @Nonnull Variable param, @Nonnegative int parameterAnnotationIdx) throws IOException {
    sourceInfoWriter.writeUnknwonDebugBegin();
    writer.writeKeyword(Token.PARAMETER);
    writer.writeOpen();
    writer.writeId(param.getId());
    writer.writeInt(NO_MODIFIER);
    writer.writeId(paramType.getDescriptor());
    writer.writeString(param.getName());
    annotWriter.writeAnnotations(currentMethod, parameterAnnotationIdx);
    writer.writeOpenNodeList();
    if (param.hasSignature()) {
      writer.writeKeyword(Token.GENERIC_SIGNATURE); // Marker generic signature
      writer.writeOpen();
      writer.writeString(param.getSignature());
      writer.writeClose();
    }
    writer.writeCloseNodeList();
    // TODO(mikaelpeltier) Add debug information of parameter
    sourceInfoWriter.writeUnknownDebugEnd();
    writer.writeClose();
  }

  @CheckForNull
  private LocalVariableNode getLocalVariableNode(@Nonnegative int localIdx) {
    assert localIdx >= 0;
    if (options.isEmitDebugInfo() && currentMethod.localVariables != null) {
      for (LocalVariableNode lvn : currentMethod.localVariables) {
        int startScope = currentMethod.instructions.indexOf(lvn.start) - 1;
        int endScope = currentMethod.instructions.indexOf(lvn.end);
        if (lvn.index == localIdx && currentPc >= startScope && currentPc <= endScope) {
          assert lvn.desc != null;
          return lvn;
        }
      }
    }

    return null;
  }

  private void removeDeadCode() {
    Frame<BasicValue>[] frames = analyzer.getFrames();
    AbstractInsnNode[] insns = currentMethod.instructions.toArray();
    for (int i = 0; i < frames.length; ++i) {
      if (frames[i] == null) {
        // do not remove labels, they may be used as local scope bounds or catch bounds.
        AbstractInsnNode insn = insns[i];
        if (insn instanceof LabelNode) {
          continue;
        }
        currentMethod.instructions.remove(insn);
      }
    }
  }

  private boolean isBooleanAssignIssue(@Nonnull Variable lhs, @Nonnull Variable rhs) {
    return isBooleanAssignIssue(lhs.getType(), rhs.getType());
  }

  private boolean isBooleanAssignIssue(@Nonnull Type lhs, @Nonnull Type rhs) {
    return (lhs == Type.BOOLEAN_TYPE && rhs != Type.BOOLEAN_TYPE)
        || (rhs == Type.BOOLEAN_TYPE && lhs != Type.BOOLEAN_TYPE);
  }

  @Nonnull
  private Variable getLocalVariable(@Nonnull Frame<BasicValue> frame, @Nonnegative int localIdx){
    BasicValue bv = frame.getLocal(localIdx);
    assert bv != BasicValue.UNINITIALIZED_VALUE;
    LocalVariableNode lvn = getLocalVariableNode(localIdx);
    String localName;
    String id;
    Type localType;
    String signature;
    Variable v;
    if (lvn == null) {
      id = getUnnamedLocalId(localIdx, bv.getType());
      localName = id;
      localType = typeToUntyped(bv.getType());
      signature = null;
      v = getVariable(id, localName, localType, signature);
      // Unnamed variable will be define as synthetic
      v.setSynthetic();
    } else {
      id = getNamedLocalId(lvn);
      localName = lvn.name;
      localType = Type.getType(lvn.desc);
      signature = lvn.signature;
      v = getVariable(id, localName, localType, signature);
    }
    return v;
  }

  @Nonnull
  private String getUnnamedParameterId(@Nonnegative int localIdx, @Nonnull Type localType) {
    return "-p_" + localIdx + "_" + stringLegalizer(localType.getDescriptor());
  }

  @Nonnull
  private String getUnnamedLocalId(@Nonnegative int localIdx, @Nonnull Type localType) {
    return "-l_" + localIdx + "_" + typeToUntypedDesc(localType);
  }

  @Nonnull
  private String getNamedLocalId(@Nonnull LocalVariableNode lvn) {
    return (lvn.name + "_" + lvn.index + "_" + (lvn.signature != null ?
        stringLegalizer(lvn.signature) : stringLegalizer(lvn.desc)));
  }

  @Nonnull
  private Variable getStackVariable(@Nonnull Frame<BasicValue> frame, int stackIdx){
    int stackHeight = frame.getStackSize() + stackIdx;
    BasicValue bv = frame.getStack(stackHeight);
    assert bv != BasicValue.UNINITIALIZED_VALUE;
    String id = "-s_" + stackHeight + "_" + typeToUntypedDesc(bv.getType());
    Variable variable = getVariable(id, id, typeToUntyped(bv.getType()), null);
    variable.setSynthetic();
    return variable;
  }

  @Nonnull
  private Variable getVariable(@Nonnull String id, @Nonnull String name, @Nonnull Type type,
      @CheckForNull String signature) {
    Variable var = nameToVar.get(id);

    if (var == null) {
      var = new Variable(id, name, type, signature);
      nameToVar.put(id, var);
    }

    return var;
  }

  @Nonnull
  private String typeToUntypedDesc(@Nonnull Type type) {
    if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
      return "R";
    } else if (type.getSort() == Type.BOOLEAN || type.getSort() == Type.BYTE
        || type.getSort() == Type.CHAR || type.getSort() == Type.SHORT
        || type.getSort() == Type.INT) {
      return Type.INT_TYPE.getDescriptor();
    }
    return type.getDescriptor();
  }

  @Nonnull
  private Type typeToUntyped(@Nonnull Type type) {
    if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
      return Type.getType("Ljava/lang/Object;");
    } else if (type.getSort() == Type.BOOLEAN || type.getSort() == Type.BYTE
        || type.getSort() == Type.CHAR || type.getSort() == Type.SHORT
        || type.getSort() == Type.INT) {
      return Type.INT_TYPE;
    }
    return type;
  }

  @Nonnull
  private String stringLegalizer(@Nonnull String str) {
    return str.replace('/', '_').replace(';', '_').replace('<', '_').replace('>', '_')
        .replace(':', '_');
  }
}
