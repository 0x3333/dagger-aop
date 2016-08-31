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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.x3333.dagger.aop.di.DaggerSomeComponent;
import com.github.x3333.dagger.aop.di.SomeComponent;
import com.github.x3333.dagger.aop.user.Some;
import com.github.x3333.dagger.aop.user.impl.Interceptor_SomeImpl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.stubbing.defaultanswers.ForwardsInvocations;

import com.google.common.truth.Truth;

/**
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public class InterceptorTest {

  SomeComponent component;
  Some realSome;
  Some some;

  @Before
  public void setUp() {
    component = DaggerSomeComponent.create();
    realSome = component.getSome();
    some = mock(Some.class, new ForwardsInvocations(realSome));
  }

  @Test
  public void testTypeInterception() {
    Truth.assertThat(realSome).isInstanceOf(Interceptor_SomeImpl.class);
  }

  @Test
  public void testInterception() {
    // some.doWork() return "doWork" by default.
    // when intercepted the result is prefixed with "TestInterceptor_"
    Truth.assertThat(some.doWork()).isEqualTo("TestInterceptor_doWork");
    verify(some, times(1)).doWork();
  }

}
