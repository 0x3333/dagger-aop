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

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import com.github.x3333.dagger.aop.internal.InterceptorProcessor;

import javax.tools.JavaFileObject;

import org.junit.Test;

import com.google.testing.compile.JavaFileObjects;

/**
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public class GeneratorTest {

  @Test
  public void generatorWithConstructorInterceptions() {
    final JavaFileObject sourceFile = JavaFileObjects //
        .forResource("unit/WithConstructor.java");

    final JavaFileObject generatedFile = JavaFileObjects//
        .forResource("unit/Interceptor_WithConstructor.java");

    assert_()//
        .about(javaSource())//
        .that(sourceFile)//
        .processedWith(new InterceptorProcessor())//
        .compilesWithoutError()//
        .and()//
        .generatesSources(generatedFile);
  }

  @Test
  public void generatorWithoutConstructorInterceptions() {
    final JavaFileObject sourceFile = JavaFileObjects //
        .forResource("unit/WithoutConstructor.java");

    final JavaFileObject generatedFile = JavaFileObjects//
        .forResource("unit/Interceptor_WithoutConstructor.java");

    assert_()//
        .about(javaSource())//
        .that(sourceFile)//
        .processedWith(new InterceptorProcessor())//
        .compilesWithoutError()//
        .and()//
        .generatesSources(generatedFile);
  }

  @Test
  public void generatorMultipleConstructorsInterceptions() {
    final JavaFileObject sourceFile = JavaFileObjects //
        .forResource("unit/MultipleConstructors.java");

    assert_()//
        .about(javaSource())//
        .that(sourceFile)//
        .processedWith(new InterceptorProcessor())//
        .failsToCompile()//
        .withErrorContaining("only one constructor");
  }

  @Test
  public void generatorNotAbstractInterceptions() {
    final JavaFileObject sourceFile = JavaFileObjects //
        .forResource("unit/NotAbstract.java");

    assert_()//
        .about(javaSource())//
        .that(sourceFile)//
        .processedWith(new InterceptorProcessor())//
        .failsToCompile()//
        .withErrorContaining("Classes with intercepted methods must be Abstract");
  }

}
