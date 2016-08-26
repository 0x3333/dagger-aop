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

import static com.google.common.base.Preconditions.checkState;

import java.lang.annotation.Annotation;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.google.auto.common.MoreElements;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

/**
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
@AutoValue
public abstract class MethodBind {

  private TypeElement classElement;

  TypeElement getClassElement() {
    if (classElement == null) {
      classElement = MoreElements.asType(Util.scanForElementKind(ElementKind.CLASS, getMethodElement()));
    }
    return classElement;
  }

  abstract ExecutableElement getMethodElement();

  abstract ImmutableList<Class<? extends Annotation>> getAnnotations();

  static Builder builder() {
    return new AutoValue_MethodBind.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder setMethodElement(final ExecutableElement methodElement);

    abstract ImmutableList.Builder<Class<? extends Annotation>> annotationsBuilder();

    abstract MethodBind autoBuild();

    public MethodBind build() {
      final MethodBind methodBind = autoBuild();
      checkState(methodBind.getAnnotations().size() > 0, "Method Binds must have at least one annotation!");
      return methodBind;
    }

  }

}
