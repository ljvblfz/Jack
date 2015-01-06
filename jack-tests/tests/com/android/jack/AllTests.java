package com.android.jack;

import com.android.jack.analysis.dfa.reachingdefs.ReachingDefsTests;
import com.android.jack.annotation.AnnotationTests;
import com.android.jack.arithmetic.ArithmeticTests;
import com.android.jack.array.ArrayTests;
import com.android.jack.assertion.AssertionTests;
import com.android.jack.assign.AssignTests;
import com.android.jack.box.BoxTests;
import com.android.jack.bridge.BridgeTests;
import com.android.jack.cast.CastAllTests;
import com.android.jack.classpath.ClasspathTests;
import com.android.jack.clinit.ClinitTests;
import com.android.jack.comparison.ComparisonTests;
import com.android.jack.compiletime.CompileTimeTests;
import com.android.jack.conditional.ConditionalTests;
import com.android.jack.constant.ConstantTests;
import com.android.jack.debug.DebugTests;
import com.android.jack.dextag.DexTagTests;
import com.android.jack.dx.DxTests;
import com.android.jack.enums.EnumsTests;
import com.android.jack.error.ErrorHandlingAllTests;
import com.android.jack.experimental.incremental.DependencyAllTests;
import com.android.jack.external.ExternalTests;
import com.android.jack.fibonacci.FibonacciTests;
import com.android.jack.field.FieldTests;
import com.android.jack.fileconflict.FileConflictTests;
import com.android.jack.flow.FlowTests;
import com.android.jack.frontend.MissingClassTest;
import com.android.jack.generic.basic.GenericTests;
import com.android.jack.ifstatement.IfstatementTests;
import com.android.jack.imports.ImportTests;
import com.android.jack.init.InitTests;
import com.android.jack.inner.InnerTests;
import com.android.jack.instance.InstanceTest;
import com.android.jack.invoke.InvokeTests;
import com.android.jack.jarjar.JarjarTests;
import com.android.jack.java7.Java7AllTest;
import com.android.jack.label.LabelTest;
import com.android.jack.library.LibraryTest;
import com.android.jack.lookup.LookupTests;
import com.android.jack.multidex.MultiDexAllTests;
import com.android.jack.newarray.NewarrayTests;
import com.android.jack.nopackage.NoPackageTests;
import com.android.jack.opcodes.OpcodesTests;
import com.android.jack.optimizations.exprsimplifier.ExprsimplifierTests;
import com.android.jack.optimizations.notsimplifier.NotsimplifierTests;
import com.android.jack.optimizations.uselesscopy.UselessVariableCopyTest;
import com.android.jack.order.OrderTests;
import com.android.jack.preprocessor.PreProcessorTests;
import com.android.jack.resource.ResourceTests;
import com.android.jack.returnstatement.ReturnstatementTests;
import com.android.jack.shrob.ShrobAllTests;
import com.android.jack.string.StringTests;
import com.android.jack.switchstatement.SwitchstatementTests;
import com.android.jack.synchronize.SynchronizeTests;
import com.android.jack.threeaddress.ThreeaddressTests;
import com.android.jack.throwstatement.ThrowstatementTests;
import com.android.jack.tools.merger.MergerAllTests;
import com.android.jack.trycatch.TrycatchTests;
import com.android.jack.tryfinally.TryfinallyTests;
import com.android.jack.type.TypeTests;
import com.android.jack.unary.UnaryTests;
import com.android.jack.verify.VerifyTests;
import com.android.jack.withphantom.WithPhantomTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
    AnnotationTests.class,
    ArithmeticTests.class,
    ArrayTests.class,
    AssertionTests.class,
    AssignTests.class,
    BoxTests.class,
    BridgeTests.class,
    CastAllTests.class,
    ClasspathTests.class,
    ClinitTests.class,
    ComparisonTests.class,
    CompileTimeTests.class,
    ConditionalTests.class,
    ConstantTests.class,
    DebugTests.class,
    DependencyAllTests.class,
    DexTagTests.class,
    DxTests.class,
    EnumsTests.class,
    ExternalTests.class,
    ErrorHandlingAllTests.class,
    FibonacciTests.class,
    FieldTests.class,
    FileConflictTests.class,
    FlowTests.class,
    GenericTests.class,
    JarjarTests.class,
    IfstatementTests.class,
    InitTests.class,
    InnerTests.class,
    InstanceTest.class,
    ImportTests.class,
    InvokeTests.class,
    Java7AllTest.class,
    LabelTest.class,
    LibraryTest.class,
    LookupTests.class,
    MergerAllTests.class,
    MissingClassTest.class,
    MultiDexAllTests.class,
    NewarrayTests.class,
    ExprsimplifierTests.class,
    NotsimplifierTests.class,
    NoPackageTests.class,
    OrderTests.class,
    OpcodesTests.class,
    PreProcessorTests.class,
    ReturnstatementTests.class,
    ResourceTests.class,
    ReachingDefsTests.class,
    ShrobAllTests.class,
    StringTests.class,
    SwitchstatementTests.class,
    SynchronizeTests.class,
    ThreeaddressTests.class,
    ThrowstatementTests.class,
    TrycatchTests.class,
    TryfinallyTests.class,
    TypeTests.class,
    UnaryTests.class,
    UselessVariableCopyTest.class,
    VerifyTests.class,
    WithPhantomTests.class,
  })
public class AllTests {}
