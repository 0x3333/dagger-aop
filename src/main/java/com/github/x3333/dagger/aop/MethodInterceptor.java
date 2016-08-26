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

/**
 * Basic interface for a Method Interceptor.
 * 
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public interface MethodInterceptor {

  /**
   * Called by the generated class. Responsable to implement the interceptor logic as well to call the original method using
   * {@link MethodInvocation} invocation parameter.
   * 
   * @param <T> type of the method's return value.
   * @param invocation The {@link MethodInvocation} with information about the interception.
   * @return The original method's return value, or a custom one.
   * @throws Throwable Exception thrown by the interceptor or the original method.
   */
  <T> T invoke(MethodInvocation invocation) throws Throwable;

}
