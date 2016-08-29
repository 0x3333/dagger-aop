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
import java.util.List;

/**
 * Represents a Method Invocation.
 * 
 * <p>
 * A instance of this interface is provided to {@link MethodInterceptor} to proceed the invocation when necessary.
 * 
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public interface MethodInvocation {

  /**
   * Instance of the object that has been intercepted.
   * 
   * @return Object instance.
   */
  Object getInstance();

  /**
   * Method that has been intercepted. Represents the method in the super class, not the instance itself.
   * 
   * @return Method intercepted.
   */
  Method getMethod();

  /**
   * All the annotations that the method intercepted has in runtime.
   * 
   * @return Method's annotations.
   */
  List<Annotation> getAnnotations();

  /**
   * Called by the MethodInterceptor when the original method need to proceed.
   * 
   * @return The invocated method return value, or a custom return value.
   * 
   * @throws Throwable Exception thrown by the interceptor or the original method. Will be handled by the generated class.
   */
  Object proceed() throws Throwable;

  /**
   * Return the Annotation instance of the original method.
   * 
   * <p>
   * This is a helper method, so {@link MethodInterceptor} don't need to iterate over all annotations.
   * 
   * @param <A> Type of the Annotation to be returned.
   * @param annotationClass Class of the Annotation to be returned.
   * @return Optional Annotation instance.
   */
  @SuppressWarnings("unchecked")
  default <A extends Annotation> A annotation(final Class<A> annotationClass) {
    return (A) getAnnotations().stream().filter(a -> a.annotationType().equals(annotationClass)).findFirst().get();
  }

}
