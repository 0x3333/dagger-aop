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

package com.github.x3333.dagger.aop.internal;

import java.util.Collections;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;

/**
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
@AutoService(Processor.class)
public class InterceptorProcessor extends BasicAnnotationProcessor {

  public static AnnotationSpec generatedAnnotation(final Class<?> generatorClass) {
    return AnnotationSpec.builder(Generated.class)//
        .addMember("value", "$S", generatorClass.getCanonicalName())//
        .addMember("comments", "$S", "https://github.com/0x3333/dagger-aop").build();
  }

  //

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  protected Iterable<? extends ProcessingStep> initSteps() {
    return Collections.singleton(new InterceptorProcessorStep(processingEnv));
  }

}
