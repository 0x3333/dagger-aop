package com.github.x3333.dagger.aop.test;

import com.github.x3333.dagger.aop.MethodInterceptor;
import com.github.x3333.dagger.aop.MethodInvocation;

public class Interceptor2 implements MethodInterceptor {

  @Override
  @SuppressWarnings("unchecked")
  public <T> T invoke(final MethodInvocation invocation) throws Throwable {
    return (T) invocation.proceed();
  }

}
