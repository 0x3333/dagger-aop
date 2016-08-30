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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * Helper utilities to Source Generator.
 * 
 * @author Tercio Gaudencio Filho (terciofilho [at] gmail.com)
 */
public final class SourceGenerator {

  private SourceGenerator() {}

  /**
   * Generate the {@link Generated} annotation.
   * 
   * @param generator Generator class to be set as value in the Generated annotation.
   * @return Generated annotation populated.
   */
  public static AnnotationSpec generatedAnnotation(final Class<?> generator) {
    return AnnotationSpec.builder(Generated.class)//
        .addMember("value", "$S", generator.getCanonicalName())//
        .addMember("comments", "$S", "https://github.com/0x3333/dagger-aop").build();
  }

  /**
   * Write a Class to the Processing Environment Filer.
   * 
   * @param processingEnv ProcessingEnvironment to use.
   * @param packageName Package name of the class to write.
   * @param classSpec Class to be written.
   */
  public static void writeClass(final ProcessingEnvironment processingEnv, final String packageName,
      final TypeSpec classSpec) {
    try {
      JavaFile.builder(packageName, classSpec).build().writeTo(processingEnv.getFiler());
    } catch (final IOException ioe) {
      final StringWriter sw = new StringWriter();
      try (final PrintWriter pw = new PrintWriter(sw);) {
        pw.println("Error generating source file for type " + classSpec.name);
        ioe.printStackTrace(pw);
        pw.close();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, sw.toString());
      }
    }
  }

}
