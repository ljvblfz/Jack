package com.android.jack.transformations.lambda;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.filter.TypeWithoutValidTypePrebuilt;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/** Convert lambda to anonymous class. */
@Description("Convert lambda to anonymous class implementation.")
@Constraint(need = { JLambda.class, JLambda.DefaultBridgeAddedInLambda.class,
                     LambdaInfoMarker.class })
@Transform(remove = JLambda.class,
    add = JFieldRef.class)
@Use(LambdaInfoMarker.class)
@Support(LambdaToAnonymousConverter.class)
@Access(JDefinedClassOrInterface.class)
@Filter(TypeWithoutValidTypePrebuilt.class)
public class LambdaConverter implements RunnableSchedulable<JMethod> {
  @Override
  public void run(@Nonnull JMethod method) {
    final TransformationRequest request = new TransformationRequest(method);
    JVisitor visitor = new JVisitor() {
      @Override
      public boolean visit(@Nonnull JLambda lambda) {
        LambdaInfoMarker info = lambda.getMarker(LambdaInfoMarker.class);
        assert info != null;

        // Replace a lambda expression by either a new lambda object instantiation,
        // or field load in case such a field is created for stateless lambda.
        JExpression newNode;
        if (info.hasInstanceField()) {
          // Replace the reference to a lambda class instantiation with
          // a reference to a field storing a proper lambda group instance
          JField instanceField = info.getInstanceField();
          newNode = new JFieldRef(SourceInfo.UNKNOWN, null,
              instanceField.getId(), instanceField.getEnclosingType());

        } else {
          // There is no single instance for this lambda
          newNode = info.createGroupClassInstance(
              request, lambda.getCapturedVariables(), lambda.getSourceInfo());
        }

        request.append(new Replace(lambda, newNode));
        return false;
      }
    };
    visitor.accept(method);
    request.commit();
  }
}
