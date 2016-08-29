package com.github.x3333.dagger.aop.test;

import com.github.x3333.dagger.aop.InterceptorHandler;
import com.github.x3333.dagger.aop.test.annotation.InvalidAnnotation;

import java.lang.annotation.Annotation;

public class InvalidHandler implements InterceptorHandler {

  @Override
  public Class<? extends Annotation> annotation() {
    return InvalidAnnotation.class;
  }

  @Override
  public Class<Interceptor> methodInterceptorClass() {
    return Interceptor.class;
  }

}
