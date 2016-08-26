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

import static com.github.x3333.dagger.aop.Util.scanForElementKind;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
class InterceptorProcessorStep implements BasicAnnotationProcessor.ProcessingStep {

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
    final Map<ExecutableElement, MethodBind.Builder> builders = new HashMap<>();
    for (final Class<? extends Annotation> annotation : services.keySet()) {
      final InterceptorHandler service = services.get(annotation);

      // Group by Method
      for (final Element element : elementsByAnnotation.get(annotation)) {
        if (element.getKind() != ElementKind.METHOD) {
          printWarning(element, "Ignoring element, not a Method!");
          continue;
        }
        final ExecutableElement methodElement = MoreElements.asExecutable(element);

        final TypeElement classElement = MoreElements.asType(scanForElementKind(ElementKind.CLASS, methodElement));
        if (MoreElements.isAnnotationPresent(classElement, Generated.class)) {
          printWarning(element, "Ignoring element, Generated code!");
          continue;
        }

        final String errorMessage = service.validateMethod(methodElement);
        if (errorMessage != null) {
          printError(methodElement, errorMessage);
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
    final Multimap<TypeElement, MethodBind> classes = ArrayListMultimap.create();
    builders.values().forEach(b -> {
      final MethodBind bind = b.build();
      classes.put(bind.getClassElement(), bind);
    });

    // Process binds by grouped Class
    classes.keySet().forEach(k -> processBind(k, classes.get(k)));

    // PostProcess to Handlers
    for (final Entry<Class<? extends Annotation>, InterceptorHandler> serviceEntry : services.entrySet()) {
      final Multimap<TypeElement, MethodBind> bindings =
          Multimaps.filterEntries(classes, entry -> entry.getValue().getAnnotations().contains(serviceEntry.getKey()));
      serviceEntry.getValue().postProcess(processingEnv, bindings);
    }

    return Collections.emptySet();
  }

  //

  private void processBind(final TypeElement superClassElement, final Collection<MethodBind> methodBinds) {
    final String packageName = MoreElements//
        .asPackage(scanForElementKind(ElementKind.PACKAGE, superClassElement)).getQualifiedName().toString();

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
