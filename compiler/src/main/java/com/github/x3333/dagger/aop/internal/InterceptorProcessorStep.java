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
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.github.x3333.dagger.aop.InterceptorHandler;
import com.github.x3333.dagger.aop.MethodInterceptor;
import com.github.x3333.dagger.aop.Sources;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.auto.common.Visibility;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
  private final Optional<Boolean> disableModuleGeneration;
  private final Optional<String> modulePackage;
  private final ImmutableMap<Class<? extends Annotation>, InterceptorHandler> services;
  private final InterceptorGenerator generator;

  //

  /**
   * Create a InterceptorProcessorStep instance.
   * 
   * @param processingEnv ProcessingEnvironment associated to the Processor.
   * @param generateModule If we should generate a Dagger Module for intercepted methods.
   * @param modulePackage If we should generate a Dagger Module, in which package it should be created.
   */
  public InterceptorProcessorStep(final ProcessingEnvironment processingEnv,
      final Optional<Boolean> disableModuleGeneration, final Optional<String> modulePackage) {
    this.processingEnv = processingEnv;
    this.disableModuleGeneration = disableModuleGeneration;
    this.modulePackage = modulePackage;
    final ServiceLoader<InterceptorHandler> handlers =
        ServiceLoader.load(InterceptorHandler.class, this.getClass().getClassLoader());
    this.services = Maps.uniqueIndex(handlers, InterceptorHandler::annotation);
    this.services.forEach((k, v) -> validateAnnotation(v, k));
    this.generator = new InterceptorGenerator(this.services);
  }

  //

  @Override
  public Set<? extends Class<? extends Annotation>> annotations() {
    return this.services.keySet();
  }

  @Override
  public Set<Element> process(final SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
    // No services registered, maybe a missing dependency?
    if (this.services.size() == 0) {
      printError(null,
          "No InterceptorHandler registered. Did you forgot to add some interceptor in your dependencies?");
      return Collections.emptySet();
    }

    final Map<ExecutableElement, MethodBind.Builder> builders = new HashMap<>();
    for (final Class<? extends Annotation> annotation : this.services.keySet()) {
      final InterceptorHandler service = this.services.get(annotation);

      // Group by Method
      for (final Element element : elementsByAnnotation.get(annotation)) {
        String errorMessage = validateElement(element);
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
    final Multimap<TypeElement, MethodBind> classes = TreeMultimap.create(
        Comparator.comparing(o -> o.getSimpleName().toString()),
        Comparator.comparingInt(MethodBind::getOrder));
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

    if (!this.disableModuleGeneration.isPresent() || !this.disableModuleGeneration.get()) {
      // Generate Dagger Module for intercepted Classes
      generateInterceptorModule(generatedTypes);
    }

    // PostProcess to Handlers
    for (final Entry<Class<? extends Annotation>, InterceptorHandler> serviceEntry : this.services.entrySet()) {
      final Multimap<TypeElement, MethodBind> bindings =
          Multimaps.filterEntries(classes, entry -> entry.getValue().getAnnotations().contains(serviceEntry.getKey()));
      serviceEntry.getValue().postProcess(this.processingEnv, bindings.keySet());
    }

    return Collections.emptySet();
  }

  //

  /**
   * Validates if the contract of a intercepted method has been fulfilled.
   * 
   * <p>
   * Element constraints:
   * <ul>
   * <li>Must have:
   * <ul>
   * <li>{@link ElementKind#METHOD}</li>
   * </ul>
   * </li>
   * <li>Can't have:
   * <ul>
   * <li>{@link Modifier#PRIVATE}</li>
   * <li>{@link Modifier#ABSTRACT}</li>
   * <li>{@link Modifier#STATIC}</li>
   * </ul>
   * </li>
   * </ul>
   * 
   * <p>
   * Element declaring Class constraints:
   * <ul>
   * <li>Must have:
   * <ul>
   * <li>{@link Modifier#ABSTRACT}</li>
   * <li>if Inner Class {@link Modifier#STATIC}</li>
   * </ul>
   * </li>
   * <li>Can't have:
   * <ul>
   * <li>{@link Modifier#FINAL}</li>
   * <li>{@link Generated &#064;Generated}</li>
   * <li>{@link Modifier#ABSTRACT} Method</li>
   * </ul>
   * </li>
   * </ul>
   * 
   * @param element Element to be validated.
   * @return String Error message in case the element has not passed validation, <code>null</code> if valid.
   */
  private String validateElement(final Element element) {
    final ElementKind kind = element.getKind();
    final Set<Modifier> modifiers = element.getModifiers();

    if (kind != METHOD) {
      return "Intercepted element must be a Method!";
    }
    if (modifiers.contains(PRIVATE)) {
      return "Intercepted methods cannot be Private!";
    }
    if (modifiers.contains(FINAL)) {
      return "Intercepted methods cannot be Final!";
    }
    if (modifiers.contains(ABSTRACT)) {
      return "Intercepted methods cannot be Abstract!";
    }
    if (modifiers.contains(STATIC)) {
      return "Intercepted methods cannot be Static!";
    }

    // Is inside a OuterClass or a Static InnerClass.
    final TypeElement classElement = MoreElements.asType(scanForElementKind(ElementKind.CLASS, element));

    if (ElementFilter.methodsIn(classElement.getEnclosedElements()).stream()//
        .filter(e -> e.getModifiers().contains(ABSTRACT)).count() > 0) {
      return "Classes with intercepted methods cannot have Abstract methods!";
    }

    // Cannot process already Generated Classes
    if (MoreElements.isAnnotationPresent(classElement, Generated.class)) {
      return "Generated code cannot be processed!";
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
    // If Inner Class, must be static
    if (classElement.getNestingKind() == NestingKind.MEMBER && !classElement.getModifiers().contains(Modifier.STATIC)) {
      return "Classes with intercepted methods must be Static if it's an Inner Class!";
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
   * If annotation is <em>invalid</em>, an {@link UnsupportedOperationException} will be thrown.
   * 
   * <p>
   * {@link InterceptorHandler InterceptorHandlers} annotations must have {@link Retention} set to {@link RetentionPolicy
   * RetentionPolicy.RUNTIME} and {@link Target} set to {@link ElementType ElementType.METHOD}.
   * 
   * @param service
   * 
   * @param annotation Annotation to be validated.
   */
  private void validateAnnotation(final InterceptorHandler service, final Class<? extends Annotation> annotation) {
    final Retention retention = annotation.getAnnotation(Retention.class);
    if (retention != null && retention.value() != RUNTIME) {
      throw new UnsupportedOperationException(
          String.format("%s Annotation '%s' must have Retention set to RUNTIME.", service.getClass(), annotation));
    }
    final Target target = annotation.getAnnotation(Target.class);
    if (target != null && target.value().length != 1 || target.value()[0] != ElementType.METHOD) {
      throw new UnsupportedOperationException(
          String.format("%s Annotation '%s' must have Target set to METHOD.", service.getClass(), annotation));
    }
  }

  /**
   * Process {@link MethodBind MethodBinds} using the {@link InterceptorGenerator}.
   */
  private TypeSpec processBind(final TypeElement superClassElement, final Collection<MethodBind> methodBinds) {
    final Element packageElement = scanForElementKind(ElementKind.PACKAGE, superClassElement);
    final String packageName = MoreElements.asPackage(packageElement).getQualifiedName().toString();

    final TypeSpec interceptorClass = this.generator.generateInterceptor(superClassElement, methodBinds);

    try {
      JavaFile.builder(packageName, interceptorClass).build().writeTo(this.processingEnv.getFiler());
    } catch (final IOException ioe) {
      final StringWriter sw = new StringWriter();
      try (final PrintWriter pw = new PrintWriter(sw)) {
        pw.println("Error generating source file for type " + interceptorClass.name);
        ioe.printStackTrace(pw);
        pw.close();
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, sw.toString());
      }
    }
    return interceptorClass;
  }

  private void generateInterceptorModule(final Map<TypeSpec, TypeElement> generatedTypes) {
    final String className = "InterceptorModule";
    final PackageElement pkg = this.processingEnv.getElementUtils().getPackageElement(//
        this.modulePackage.isPresent() ? this.modulePackage.get() : PACKAGE);

    final TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className) //
        .addModifiers(PUBLIC, ABSTRACT)//
        .addJavadoc("This class is the default Dagger module for Intercepted Methods.\n")//
        .addAnnotation(Sources.generatedAnnotation(InterceptorProcessorStep.class)) //
        .addAnnotation(Module.class);

    int count = 0;
    for (final Entry<TypeSpec, TypeElement> entry : generatedTypes.entrySet()) {
      final TypeElement sourceElement = entry.getValue();

      if (!isVisibleFrom(sourceElement, pkg)) {
        printWarning(sourceElement,
            "Could not create InterceptorModule bind, source class is not visible outside its package!");
        continue;
      }
      count++;

      final ClassName sourceClassName = ClassName.get(sourceElement);
      final ClassName superClassName = ClassName.get(sourceClassName.packageName(), entry.getKey().name);

      final MethodSpec method = MethodSpec.methodBuilder("provides" + sourceElement.getSimpleName())//
          .addModifiers(ABSTRACT)//
          .addAnnotation(Binds.class)//
          .returns(sourceClassName)//
          .addParameter(superClassName, "impl", FINAL)//
          .build();
      classBuilder.addMethod(method);
    }

    if (count > 0) {
      Sources.writeClass(//
          this.processingEnv, //
          pkg.getQualifiedName().toString(), //
          classBuilder.build());
    }
  }

  /*
   * Copyright (C) 2014 Thomas Broyer - bullet - https://github.com/tbroyer/bullet
   */
  private boolean isVisibleFrom(final Element target, final PackageElement from) {
    switch (Visibility.effectiveVisibilityOfElement(target)) {
      case PUBLIC:
        return true;
      case PROTECTED:
      case DEFAULT:
        return MoreElements.getPackage(target).equals(from);
      case PRIVATE:
        return false;
      default:
        throw new AssertionError();
    }
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
    this.processingEnv.getMessager().printMessage(Kind.ERROR, message, element);
  }

  /**
   * Print a Warning message to Processing Environment.
   *
   * @param element Element that generated the message.
   * @param message Message to be printed.
   */
  private void printWarning(final Element element, final String message) {
    this.processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, message, element);
  }

}
