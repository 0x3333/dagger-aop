package com.github.x3333.dagger.aop.test;

import com.github.x3333.dagger.aop.InterceptorHandler;
import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation2;

import java.lang.annotation.Annotation;

public class ValidHandler2 implements InterceptorHandler {

  @Override
  public Class<? extends Annotation> annotation() {
    return ValidAnnotation2.class;
  }

  @Override
  public Class<Interceptor2> methodInterceptorClass() {
    return Interceptor2.class;
  }

}
