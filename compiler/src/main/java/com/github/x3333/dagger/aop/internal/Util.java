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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

/**
 * Utilities for handling types in annotation processors.
 * 
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
final class Util {

  /**
   * Return the nearest {@link Element} of Kind <code>kind</code> in the {@link Element#getEnclosingElement() enclosing elements}.
   * 
   * @param kind Kind of the element to scan.
   * @param element Element that will be scanned.
   * @return Nearest Element.
   */
  static Element scanForElementKind(final ElementKind kind, Element element) {
    while (element != null && element.getKind() != kind) {
      element = element.getEnclosingElement();
    }
    return element;
  }

  /**
   * Convert a {@link AnnotationMirror}s to {@link AnnotationSpec}.
   */
  static Iterable<AnnotationSpec> toSpec(final Iterable<? extends AnnotationMirror> mirrors) {
    return Iterables.transform(mirrors, AnnotationSpec::get);
  }

  /**
   * Convert a {@link Iterable} of {@link Element} to a {@link Iterable} of {@link String} using {@link Element#getSimpleName()}.
   */
  static Iterable<String> simpleNames(final Iterable<? extends Element> elements) {
    return Iterables.transform(elements, e -> e.getSimpleName().toString());
  }

  /**
   * Clone a Constructor {@link ExecutableElement} into a {@link MethodSpec.Builder} instance.
   * 
   * <p>
   * The cloned constructor will call super as first statement.
   * 
   * <p>
   * These elements will be cloned:
   * <ul>
   * <li>Annotations</li>
   * <li>Modifiers</li>
   * <li>Parameters</li>
   * <li>Exceptions</li>
   * </ul>
   */
  static MethodSpec.Builder cloneConstructor(final ExecutableElement element) {
    checkArgument(element.getKind() == ElementKind.CONSTRUCTOR);

    final Set<Modifier> modifiers = element.getModifiers();
    final List<ParameterSpec> parameters = Lists.transform(element.getParameters(), Util::cloneParameter);
    final List<TypeName> exceptions = Lists.transform(element.getThrownTypes(), TypeName::get);
    final Iterable<AnnotationSpec> annotations = toSpec(element.getAnnotationMirrors());

    return MethodSpec.constructorBuilder() //
        .addAnnotations(annotations) //
        .addModifiers(modifiers) //
        .addParameters(parameters) //
        .addExceptions(exceptions) //
        .addStatement("super($L)", Joiner.on(", ").join(simpleNames(element.getParameters())));
  }

  /**
   * Clone a Method {@link ExecutableElement} into a {@link MethodSpec.Builder} instance.
   * 
   * <p>
   * These elements will be cloned:
   * <ul>
   * <li>Modifiers</li>
   * <li>Parameters</li>
   * <li>Exceptions</li>
   * </ul>
   * 
   * <p>
   * Annotations aren't cloned.
   */
  static MethodSpec.Builder cloneMethod(final ExecutableElement element) {
    checkArgument(element.getKind() == ElementKind.METHOD);

    final String name = element.getSimpleName().toString();
    final Set<Modifier> modifiers = element.getModifiers();
    final List<ParameterSpec> parameters = Lists.transform(element.getParameters(), Util::cloneParameter);
    final List<TypeName> exceptions = Lists.transform(element.getThrownTypes(), TypeName::get);
    final TypeName returnType = TypeName.get(element.getReturnType());

    return MethodSpec.methodBuilder(name) //
        .addModifiers(modifiers) //
        .addParameters(parameters) //
        .addExceptions(exceptions) //
        .returns(returnType);
  }

  /**
   * Clone a Parameter {@link VariableElement} into a {@link ParameterSpec} instance.
   * 
   * <p>
   * These elements will be cloned:
   * <ul>
   * <li>Type</li>
   * <li>Name</li>
   * <li>Modifiers</li>
   * <li>Annotations</li>
   * </ul>
   */
  static ParameterSpec cloneParameter(final VariableElement element) {
    checkArgument(element.getKind() == ElementKind.PARAMETER);

    final TypeName type = TypeName.get(element.asType());
    final String name = element.getSimpleName().toString();
    final Modifier[] modifiers = element.getModifiers().toArray(new Modifier[element.getModifiers().size()]);
    final Iterable<AnnotationSpec> annotations = toSpec(element.getAnnotationMirrors());

    return ParameterSpec.builder(type, name, modifiers).addAnnotations(annotations).build();
  }

}
