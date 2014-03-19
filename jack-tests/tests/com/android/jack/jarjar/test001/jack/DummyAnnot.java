package com.android.jack.jarjar.test001.jack;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DummyAnnot {
  String value() default "";
}
