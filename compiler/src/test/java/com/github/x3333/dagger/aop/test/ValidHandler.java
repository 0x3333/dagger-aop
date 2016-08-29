package com.github.x3333.dagger.aop.test;

import com.github.x3333.dagger.aop.InterceptorHandler;
import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation;

import java.lang.annotation.Annotation;

public class ValidHandler implements InterceptorHandler {

  @Override
  public Class<? extends Annotation> annotation() {
    return ValidAnnotation.class;
  }

  @Override
  public Class<Interceptor> methodInterceptorClass() {
    return Interceptor.class;
  }

}
