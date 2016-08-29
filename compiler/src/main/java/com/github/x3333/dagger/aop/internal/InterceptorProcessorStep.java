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

import static com.github.x3333.dagger.aop.internal.Util.scanForElementKind;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import com.github.x3333.dagger.aop.InterceptorHandler;
import com.github.x3333.dagger.aop.MethodInterceptor;
import com.github.x3333.dagger.aop.SourceGenerator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import dagger.Binds;
import dagger.Module;

/**
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
class InterceptorProcessorStep implements BasicAnnotationProcessor.ProcessingStep {

  private static final String PACKAGE = MethodInterceptor.class.getPackage().getName();

  private final ProcessingEnvironment processingEnv;
  private final ImmutableMap<Class<? extends Annotation>, InterceptorHandler> services;
  private final InterceptorGenerator generator;

  //

  /**
   * Create a InterceptorProcessorStep instance.
   * 
   * @param processingEnv ProcessingEnvironment associated to the Processor.
   */
  public InterceptorProcessorStep(final ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;

    final Builder<Class<? extends Annotation>, InterceptorHandler> builder = ImmutableMap.builder();
    ServiceLoader//
        .load(InterceptorHandler.class, this.getClass().getClassLoader())//
        .forEach(service -> builder.put(service.annotation(), service));

    services = builder.build();
    generator = new InterceptorGenerator(services);
  }

  //

  @Override
  public Set<? extends Class<? extends Annotation>> annotations() {
    return services.keySet();
  }

  @Override
  public Set<Element> process(final SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
    if (services.size() == 0) {
      printError(null,
          "No InterceptorHandler registered. Did you forgot to add some interceptor in your dependencies?");
    }
    final Map<ExecutableElement, MethodBind.Builder> builders = new HashMap<>();
    for (final Class<? extends Annotation> annotation : services.keySet()) {
      final InterceptorHandler service = services.get(annotation);

      String errorMessage = validateAnnotation(service, annotation);
      if (errorMessage != null) {
        printWarning(null, errorMessage);
        continue;
      }

      // Group by Method
      for (final Element element : elementsByAnnotation.get(annotation)) {
        errorMessage = validateElement(element);
        if (errorMessage != null) {
          printError(element, errorMessage);
          continue;
        }
        final ExecutableElement methodElement = MoreElements.asExecutable(element);

        errorMessage = service.validateMethod(methodElement);
        if (errorMessage != null) {
          printError(element, errorMessage);
          continue;
        }

        builders
            .computeIfAbsent(//
                methodElement, //
                key -> MethodBind.builder().setMethodElement(methodElement))//
            .annotationsBuilder().add(annotation);
      }
    }

    // Group by Class
    // Tree map to order methods in the same order they appear in the source code.
    final Multimap<TypeElement, MethodBind> classes = TreeMultimap.create(//
        (o1, o2) -> o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString()), //
        (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
    builders.values().forEach(b -> {
      final MethodBind bind = b.build();
      classes.put(bind.getClassElement(), bind);
    });

    // Process binds by grouped Class
    final Map<TypeSpec, TypeElement> generatedTypes = new HashMap<>();
    for (final TypeElement element : classes.keySet()) {
      final TypeSpec generatedType = processBind(element, classes.get(element));
      generatedTypes.put(generatedType, element);
    }

    // Generate Dagger Module for intercepted Classes
    generateInterceptorModule(processingEnv, generatedTypes);

    // PostProcess to Handlers
    for (final Entry<Class<? extends Annotation>, InterceptorHandler> serviceEntry : services.entrySet()) {
      final Multimap<TypeElement, MethodBind> bindings =
          Multimaps.filterEntries(classes, entry -> entry.getValue().getAnnotations().contains(serviceEntry.getKey()));
      serviceEntry.getValue().postProcess(processingEnv, bindings.keySet());
    }

    return Collections.emptySet();
  }

  //

  /**
   * Validates if the contract of a intercepted method has been fulfilled.
   * 
   * @param element Element to be validated.
   * @return String Error message in case the element has not passed validation, <code>null</code> if valid.
   */
  private String validateElement(final Element element) {
    final ElementKind kind = element.getKind();
    final Set<Modifier> modifiers = element.getModifiers();

    // Is a method
    if (kind != ElementKind.METHOD) {
      return "Intercepted element must be a Method!";
    }
    // Is not private
    if (modifiers.contains(Modifier.PRIVATE)) {
      return "Intercepted methods cannot be Private!";
    }
    // Is not abstract
    if (modifiers.contains(Modifier.ABSTRACT)) {
      return "Intercepted methods cannot be Abstract!";
    }
    // Is not static
    if (modifiers.contains(Modifier.STATIC)) {
      return "Intercepted methods cannot be Static!";
    }

    // Is inside a OuterClass or a Static InnerClass.
    final TypeElement classElement = MoreElements.asType(scanForElementKind(ElementKind.CLASS, element));

    // Cannot process already Generated Classes
    if (MoreElements.isAnnotationPresent(classElement, Generated.class)) {
      // FIXME: This should be just a warning, not an error.
      return "Ignoring element, Generated code!";
    }
    // If Inner Class, must be static
    if (classElement.getNestingKind() == NestingKind.MEMBER && !classElement.getModifiers().contains(Modifier.STATIC)) {
      return "Classes with intercepted methods must be Static if it's an Inner Class!";
    }
    // Cannot be Final
    if (classElement.getModifiers().contains(Modifier.FINAL)) {
      return "Classes with intercepted methods cannot be Final!";
    }
    // Must be Abstract
    // This is to avoid user instantiating it instead the generated version of the class
    if (!classElement.getModifiers().contains(Modifier.ABSTRACT)) {
      return "Classes with intercepted methods must be Abstract!";
    }
    // Must have one constructor or no constructor at all
    if (classElement.getEnclosedElements().stream()
        .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.CONSTRUCTOR).count() > 1) {
      return "Classes with intercepted methods must have only one constructor!";
    }

    return null;
  }

  /**
   * Validate a {@link InterceptorHandler} annotation.
   * 
   * <p>
   * {@link InterceptorHandler InterceptorHandlers} annotations must have {@link Retention} set to {@link RetentionPolicy
   * RetentionPolicy.RUNTIME} and {@link Target} set to {@link ElementType ElementType.METHOD}.
   * 
   * @param service
   * 
   * @param annotation Annotation to be validated.
   * @return true if valid, false otherwise.
   */
  private String validateAnnotation(final InterceptorHandler service, final Class<? extends Annotation> annotation) {
    final Retention retention = annotation.getAnnotation(Retention.class);
    if (retention != null && retention.value() != RUNTIME) {
      return "InterceptorHandler annotation must have Retention set to RUNTIME. Ignoring " //
          + service.getClass().toString();
    }
    final Target target = annotation.getAnnotation(Target.class);
    if (target != null && target.value().length != 1 || target.value()[0] != ElementType.METHOD) {
      return "InterceptorHandler annotation must have Target set to METHOD. Ignoring " //
          + service.getClass().toString();
    }

    return null;
  }

  /**
   * Process {@link MethodBind MethodBinds} using the {@link InterceptorGenerator}.
   */
  private TypeSpec processBind(final TypeElement superClassElement, final Collection<MethodBind> methodBinds) {
    final Element packageElement = scanForElementKind(ElementKind.PACKAGE, superClassElement);
    final String packageName = MoreElements.asPackage(packageElement).getQualifiedName().toString();

    final TypeSpec interceptorClass = generator.generateInterceptor(superClassElement, methodBinds);

    try {
      JavaFile.builder(packageName, interceptorClass).build().writeTo(processingEnv.getFiler());
    } catch (final IOException ioe) {
      final StringWriter sw = new StringWriter();
      try (final PrintWriter pw = new PrintWriter(sw);) {
        pw.println("Error generating source file for type " + interceptorClass.name);
        ioe.printStackTrace(pw);
        pw.close();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, sw.toString());
      }
    }
    return interceptorClass;
  }

  private void generateInterceptorModule(final ProcessingEnvironment processingEnv,
      final Map<TypeSpec, TypeElement> generatedTypes) {
    final String className = "InterceptorModule";

    final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className) //
        .addModifiers(PUBLIC, ABSTRACT)//
        .addJavadoc("This class is the default Dagger module for Intercepted Methods.\n")//
        .addAnnotation(Module.class);

    for (final Entry<TypeSpec, TypeElement> entry : generatedTypes.entrySet()) {

      final ClassName sourceClass = ClassName.get(entry.getValue());
      final ClassName superClass = ClassName.get(sourceClass.packageName(), entry.getKey().name);

      final MethodSpec method = MethodSpec.methodBuilder("providesJpaService")//
          .addModifiers(ABSTRACT)//
          .addAnnotation(Binds.class)//
          .returns(sourceClass)//
          .addParameter(superClass, "impl", FINAL)//
          .build();
      classBuilder.addMethod(method);
    }

    SourceGenerator.writeClass(//
        processingEnv, //
        PACKAGE, //
        classBuilder.build());
  }

  /**
   * Print an Error message to Processing Environment.
   *
   * <p>
   * <strong><em>This will just print the message, callers must stop processing in case of failure.</em></strong>
   *
   * @param element Element that generated the message.
   * @param message Message to be printed.
   */
  private void printError(final Element element, final String message) {
    processingEnv.getMessager().printMessage(Kind.ERROR, message, element);
  }

  /**
   * Print a Warning message to Processing Environment.
   *
   * @param element Element that generated the message.
   * @param message Message to be printed.
   */
  private void printWarning(final Element element, final String message) {
    processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, message, element);
  }

}
