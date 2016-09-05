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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;

/**
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
@AutoService(Processor.class)
public class InterceptorProcessor extends BasicAnnotationProcessor {

  protected static String OPTION_DISABLE_DAGGER_MODULE = "aop.disable.module.generation";
  protected static String OPTION_DAGGER_MODULE_PACKAGE = "aop.module.package";

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedOptions() {
    return Sets.newHashSet(OPTION_DISABLE_DAGGER_MODULE, OPTION_DAGGER_MODULE_PACKAGE);
  }

  @Override
  protected Iterable<? extends ProcessingStep> initSteps() {
    final Optional<Boolean> disableModuleGeneration = getBooleanOption(OPTION_DISABLE_DAGGER_MODULE);
    final Optional<String> modulePackage = getOption(OPTION_DAGGER_MODULE_PACKAGE);

    return Collections.singleton(new InterceptorProcessorStep(processingEnv, disableModuleGeneration, modulePackage));
  }

  protected Optional<String> getOption(final String option) {
    checkNotNull(option);
    return Optional.ofNullable(processingEnv.getOptions().get(option));
  }

  protected Optional<Boolean> getBooleanOption(final String option) {
    checkNotNull(option);
    final String value = processingEnv.getOptions().get(option);
    return Optional.ofNullable(value == null ? null : Boolean.valueOf(value));
  }

}
