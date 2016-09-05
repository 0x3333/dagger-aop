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

import static com.github.x3333.dagger.aop.internal.Util.cloneConstructor;
import static com.github.x3333.dagger.aop.internal.Util.cloneMethod;
import static com.github.x3333.dagger.aop.internal.Util.simpleNames;
import static com.github.x3333.dagger.aop.internal.Util.toSpec;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;

import com.github.x3333.dagger.aop.AbstractMethodInvocation;
import com.github.x3333.dagger.aop.InterceptorHandler;
import com.github.x3333.dagger.aop.MethodInterceptor;
import com.github.x3333.dagger.aop.Sources;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.google.auto.common.MoreElements;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Generate interceptors classes.
 * 
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
class InterceptorGenerator {

  private static final String METHOD_CACHE_SUFIX = "Cache$";
  private static final String ANNOTATIONS_CACHE_SUFIX = "AnnotationsCache$";
  private static final String INTERCEPTOR_CLASS_PREFIX = "Interceptor_";

  //

  private final ImmutableMap<Class<? extends Annotation>, InterceptorHandler> services;

  //

  public InterceptorGenerator(final ImmutableMap<Class<? extends Annotation>, InterceptorHandler> services) {
    this.services = services;
  }

  //

  /**
   * Generate the Interceptor for the {@link TypeElement superClassElement} using {@link MethodBind methodBinds}.
   * 
   * @param superClassElement Element with method(s) annotated by some annotation.
   * @param methodBinds Method binds inside this <code>superClassElement</code>.
   * @return Generated TypeSpec.
   */
  TypeSpec generateInterceptor(final TypeElement superClassElement, final Collection<MethodBind> methodBinds) {
    final ClassName superClassElementName = ClassName.get(superClassElement);
    final String interceptorName = INTERCEPTOR_CLASS_PREFIX + Joiner.on("_").join(superClassElementName.simpleNames());

    final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(interceptorName) //
        .addOriginatingElement(superClassElement) //

        .superclass(ClassName.get(superClassElement)) //

        .addAnnotation(Sources.generatedAnnotation(InterceptorProcessor.class)) //
        .addAnnotations(toSpec(superClassElement.getAnnotationMirrors())) //

        .addModifiers(PUBLIC, FINAL);

    // Constructor
    ExecutableElement constructorElement = null;
    for (final Element el : superClassElement.getEnclosedElements()) {
      if (el.getKind() == CONSTRUCTOR) {
        constructorElement = MoreElements.asExecutable(el);
        break;
      }
    }
    final MethodSpec.Builder constructor;
    if (constructorElement == null) {
      constructor = MethodSpec.constructorBuilder().addModifiers(PUBLIC);
    } else {
      constructor = cloneConstructor(constructorElement);
    }
    constructor.addAnnotation(Inject.class);

    final CodeBlock.Builder constructorCode = CodeBlock.builder();

    // Start Annotation Cache statements.
    final CodeBlock.Builder annotationsCode = CodeBlock.builder();
    final List<Class<? extends Annotation>> interceptorsCreated = new ArrayList<>();

    final CodeBlock.Builder staticAnnotationsCacheCode = CodeBlock.builder()//
        .add("try {\n").indent();

    // Methods
    for (final MethodBind methodBind : methodBinds) {
      final ExecutableElement methodElement = methodBind.getMethodElement();
      final ImmutableList<Class<? extends Annotation>> annotations = methodBind.getAnnotations();

      final TypeMirror returnType = methodElement.getReturnType();
      final boolean hasReturnValue = returnType.getKind() != VOID;

      // proceedCall
      final String methodName = methodElement.getSimpleName().toString();
      final Iterable<String> parameterNames = simpleNames(methodElement.getParameters());
      final String joinedParameterNames = Joiner.on(", ").join(parameterNames);
      // proceedMethod
      final TypeName returnTypeName = TypeName.get(returnType);
      final String proceedMethodModifier = hasReturnValue ? "public" : "protected";
      final String proceedMethodName = hasReturnValue ? "proceed" : "noReturnProceed";
      // interceptorInvoke
      final String interceptorInvokePrefix = hasReturnValue ? "return " : "";
      final String annotationsFieldName = methodName + ANNOTATIONS_CACHE_SUFIX;
      final String methodCacheFieldName = methodName + METHOD_CACHE_SUFIX;

      // tryBlock
      final CodeBlock.Builder tryBlock = CodeBlock.builder()//
          .add("try {\n") //
          .indent();

      // proceedCall
      CodeBlock lastInvoke = CodeBlock.builder()
          .addStatement("$L$L.super.$L($L)", //
              interceptorInvokePrefix, interceptorName, //
              methodName, //
              joinedParameterNames)
          .build();

      // Process Annotations
      for (int i = 0; i < annotations.size(); i++) {
        final Class<? extends Annotation> annotation = annotations.get(i);
        final InterceptorHandler handler = services.get(annotation);
        final Class<? extends MethodInterceptor> handlerClass = handler.methodInterceptorClass();
        final String interceptorFieldName = "$interceptor" + annotation.getSimpleName();

        if (!interceptorsCreated.contains(annotation)) {
          final TypeName handlerTypeName = TypeName.get(handlerClass);
          classBuilder.addField(handlerTypeName, interceptorFieldName, PRIVATE, FINAL);
          constructor.addParameter(handlerTypeName, interceptorFieldName, FINAL);
          constructorCode.addStatement("this.$L = $L", interceptorFieldName, interceptorFieldName);
          interceptorsCreated.add(annotation);
        }

        final CodeBlock invokeMethod =
            createInvokeMethod(proceedMethodModifier, returnTypeName, proceedMethodName, lastInvoke);
        lastInvoke = createInterceptorInvoke(interceptorInvokePrefix, interceptorFieldName, interceptorName,
            methodCacheFieldName, annotationsFieldName, invokeMethod);
      }

      tryBlock.add(lastInvoke).unindent();

      final List<? extends TypeMirror> thrownTypes = methodElement.getThrownTypes();
      for (final TypeMirror thrownType : thrownTypes) {
        tryBlock.add("} catch ($T e) {\n", thrownType).indent()//
            .addStatement("throw e").unindent();
      }

      tryBlock.add("} catch (Throwable e) {\n").indent()//
          .addStatement("throw new RuntimeException(e)").unindent()//
          .add("}\n");

      final CodeBlock argumentsBlock = CodeBlock.builder()//
          .addStatement("final $T[] arguments = new $T[] { $L }", Object.class, Object.class, joinedParameterNames)//
          .build();

      final MethodSpec method = cloneMethod(MoreElements.asExecutable(methodElement))//
          .addAnnotation(Override.class)//
          .addCode(argumentsBlock)//
          .addCode(tryBlock.build())//
          .build();

      classBuilder.addMethod(method);

      // Method Cache Field
      classBuilder.addField(//
          TypeName.get(Method.class), //
          methodCacheFieldName, //
          PRIVATE, //
          STATIC, //
          FINAL);

      // Method Annotations Cache Field
      classBuilder.addField(//
          ParameterizedTypeName.get(List.class, Annotation.class), //
          annotationsFieldName, //
          PRIVATE, //
          STATIC, //
          FINAL);

      // Method Annotations Cache Statements
      final List<TypeName> parametersTypes =
          Lists.transform(methodElement.getParameters(), p -> TypeName.get(p.asType()));
      final CodeBlock parameters = parametersTypes.size() == 0 ? //
          CodeBlock.of("") //
          : CodeBlock.builder().add(//
              Strings.repeat(", $T.class", parametersTypes.size()), parametersTypes.toArray()).build();
      staticAnnotationsCacheCode//
          .addStatement("$L = $L.class.getSuperclass().getDeclaredMethod($S$L)", //
              methodCacheFieldName, //
              interceptorName, //
              methodName, parameters)//

          .addStatement("$L = $T.asList($L.getAnnotations())", //
              annotationsFieldName, //
              Arrays.class, //
              methodCacheFieldName);
    }

    constructorCode//
        .add(annotationsCode.build());

    staticAnnotationsCacheCode.unindent()//
        .add("} catch (NoSuchMethodException | SecurityException e) {\n").indent()//
        .addStatement("throw new RuntimeException(e)").unindent()//
        .add("}\n");

    constructor.addCode(constructorCode.build());

    classBuilder.addStaticBlock(staticAnnotationsCacheCode.build());

    // Add constructor at the end so methods can create the annotation cache.
    classBuilder.addMethod(constructor.build());

    return classBuilder.build();
  }

  private CodeBlock createInvokeMethod(//
      final String proceedMethodModifier, //
      final TypeName returnTypeName, //
      final String proceedMethodName, //
      final CodeBlock proceedCall) {

    return CodeBlock.builder()//
        .add("@$T\n", Override.class)//
        .add("$L $T $L() throws $T {\n", proceedMethodModifier, returnTypeName, proceedMethodName, Throwable.class)
        .indent()//
        .add(proceedCall).unindent()//
        .add("}\n")//
        .build();
  }

  private static CodeBlock createInterceptorInvoke(//
      final String prefix, //
      final String interceptorFieldName, //
      final String interceptorName, //
      final String methodCacheFieldName, //
      final String annotationsFieldName, //
      final CodeBlock method) {

    return CodeBlock.builder()//
        .add("$L$L.invoke(new $T(\n", prefix, interceptorFieldName, AbstractMethodInvocation.class).indent().indent()//
        .add("$L.this, \n", interceptorName) //
        .add("$L.$L, \n", interceptorName, methodCacheFieldName) //
        .add("arguments, \n") //
        .add("$L.$L) {\n", interceptorName, annotationsFieldName).unindent()//
        .add(method).unindent()//
        .add("});\n")//
        .build();
  }

}
