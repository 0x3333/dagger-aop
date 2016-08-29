/*
 * Copyright (C) 2016 Tercio Gaudencio Filho
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.github.x3333.dagger.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Abstract implementation for method invocations.
 * 
 * <p>
 * Implementers must implement at least one of these:
 * <ul>
 * <li>{@link #proceed()} for methods <em>with</em> a return value</li>
 * <li>{@link #noReturnProceed()} for methods <em>without</em> a return value</li>
 * </ul>
 * If implementers fail to follow the contract, a {@link RuntimeException} will be thrown.
 * 
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public abstract class AbstractMethodInvocation implements MethodInvocation {

  private final Object instance;
  private final Method method;
  private final Iterable<Annotation> annotations;

  public AbstractMethodInvocation(final Object instance, final Method method, final Iterable<Annotation> annotations) {
    this.instance = instance;
    this.method = method;
    this.annotations = annotations;
  }

  @Override
  public Object getInstance() {
    return instance;
  }

  @Override
  public Method getMethod() {
    return method;
  }

  @Override
  public Iterable<Annotation> getAnnotations() {
    return annotations;
  }

  @Override
  public Object proceed() throws Throwable {
    noReturnProceed();
    return null;
  }

  protected void noReturnProceed() throws Throwable {
    throw new RuntimeException("Invalid AbstractMethodInvocation implementation!");
  }

}
