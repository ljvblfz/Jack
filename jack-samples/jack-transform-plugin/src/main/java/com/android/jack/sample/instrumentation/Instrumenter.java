/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.sample.instrumentation;

import com.android.jack.Jack;
import com.android.jack.ir.ast.*;
import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.MethodFormatter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.request.TransformationStep;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * The class that instruments compiled methods by inserting statements that
 * print the method's name at runtime.
 * <p>
 * Like any schedulable, this class is annotated with the mandatory {@link Description} annotation.
 * <p>
 * This transformation will procude new IR nodes. We explicitly enumerate the nodes that are
 * added with the {@link Transform} annotation.
 * Besides, this transformation does not generate code in {@code three-address-code} form. Therefore
 * we need to add a {@link Constraint} annotation to specify that this schedulable can be executed
 * only when the IR is <b>not</b> in {@code three-address-code} form using the {@code no}
 * constraint.
 * Note that an alternative version of this plugin demonstrates how to apply the same transformation
 * in {@code three-address-code} form.
 * <p>
 * Finally, we want that this transformation is only applied on classes coming from the source code,
 * not from the libraries (even imported ones). Therefore we annotate this class with
 * the {@link Filter} annotation and use the {@link SourceTypeFilter} class.
 */
@Description("Instrument a method by inserting statement to print its name")
@Transform(
        add = {JMethodCall.class, JFieldRef.class, JStatement.class},
        remove = ThreeAddressCodeForm.class)
@Filter(SourceTypeFilter.class)
public class Instrumenter implements RunnableSchedulable<JMethod> {

  /**
   * This formatter is used to compute method name following the binary name convention.
   */
  @Nonnull
  private final TypeAndMethodFormatter formatter = BinarySignatureFormatter.getFormatter();

  /**
   * The class defining <code>java.io.PrintStream</code> in the internal representation.
   */
  @Nonnull
  private final JDefinedClass javaLangSystemClass;

  /**
   * The class defining <code>java.io.PrintStream</code> in the internal representation.
   */
  @Nonnull
  private final JDefinedClass javaIoPrintStreamClass;

  /**
   * The field <code>java.lang.System.out</code> in the internal representation.
   */
  @Nonnull
  private final JFieldId systemOutFieldId;

  /**
   * The method <code>java.io.PrintStream.println(java.lang.String)</code> in the internal
   * representation.
   */
  @Nonnull
  private final JMethodId printlnMethod;

  /**
   * The constructor used by the scheduler to build an instance of this schedulable (using
   * reflection).
   */
  public Instrumenter() {
    // Get the lookup manager.
    JNodeLookup lookup = Jack.getSession().getLookup();

    // Lookup java.lang.System.
    javaLangSystemClass = lookup.getClass("Ljava/lang/System;");

    // Lookup java.io.PrintStream, the type of the System.out field.
    javaIoPrintStreamClass = lookup.getClass("Ljava/io/PrintStream;");

    // Lookup static field 'out' in java.lang.System
    systemOutFieldId = javaLangSystemClass.getFieldId("out", javaIoPrintStreamClass,
            FieldKind.STATIC);

    // We look for java.io.PrintStream.println(java.lang.String)
    JType javaLangStringClass = lookup.getClass(CommonTypes.JAVA_LANG_STRING);
    List<JType> methodArgTypes = Collections.singletonList(javaLangStringClass);
    printlnMethod = javaIoPrintStreamClass.getMethodId("println", methodArgTypes,
            MethodKind.INSTANCE_VIRTUAL, JPrimitiveType.JPrimitiveTypeEnum.VOID.getType());

  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract()) {
      // We do not instrument method with no code.
      return;
    }

    // Create the statement representing "System.out.println(<method_name>)" in memory.
    JStatement printStatement = createPrintlnStatement(method);

    // The statement is not attached to the existing IR. We need to insert it at the beginning
    // of the method.

    // Create a transformation request that will contain our transformation.
    TransformationRequest transformationRequest = new TransformationRequest(method);

    // The method is not native so the cast is valid.
    JMethodBody methodBody = (JMethodBody) method.getBody();

    // Append a transformation step that insert our statement in the method.
    TransformationStep ts = new PrependStatement(methodBody.getBlock(), printStatement);
    transformationRequest.append(ts);

    // Commit the changes to update the IR of the method.
    transformationRequest.commit();
  }

  private String getMethodName(@Nonnull JMethod method) {
    StringBuilder sb = new StringBuilder();
    sb.append(formatter.getName(method.getEnclosingType()));
    sb.append(formatter.getName(method));
    return sb.toString();
  }

  /**
   * This method creates a statement that represents <code>System.out.println(method_name)</code>.
   * This statement is not inserted in the IR at this stage.
   *
   * @return a {@link JStatement} representing the method call.
   */
  private JStatement createPrintlnStatement(@Nonnull JMethod method) {
    // SourceInfo is a very important part when transforming the IR. It allows Jack to generate
    // correct debug info, even when generating synthetic code.
    // When a transformation creates new nodes in the IR that are related to existing code (like
    // splitting an expression into multiple ones), it is good to attach the same source info to
    // these nodes.
    // On the other hand, if the transformation creates new nodes that are NOT related to the
    // existing code, it is better to use SourceInfo.UNKNOWN to indicate they are not coming from
    // actual source code.
    // In our example, we create a statement that does not exist at all in the source code.
    // Therefore, our nodes will be set to SourceInfo.UNKNOWN.
    SourceInfo sourceInfo = SourceInfo.UNKNOWN;

    // We compute the method name using the Jack formatter APIs.
    String methodName = getMethodName(method);

    // Create the string constant containing the method name.
    JStringLiteral methodNameString = new JStringLiteral(sourceInfo, methodName);

    // Create the expression "java.lang.System.out".
    JFieldRef systemOutFieldAccess = new JFieldRef(sourceInfo, null /* static access */,
            systemOutFieldId, javaLangSystemClass);

    // Create the method call to java.io.PrintStream.println(java.lang.String) that takes
    // "System.out" as receiver. In other words, we create the expression
    // "System.out.println(<methodNameString>)".
    JMethodCall methodCall = new JMethodCall(sourceInfo, systemOutFieldAccess,
            javaIoPrintStreamClass, printlnMethod.getMethodIdWide(), printlnMethod.getType(),
            true /* virtual method */);
    methodCall.addArg(methodNameString);

    // The root expression of the statement is the method call.
    return new JExpressionStatement(methodCall.getSourceInfo(), methodCall);
  }
}
