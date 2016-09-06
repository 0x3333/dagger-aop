# Dagger2 AOP

### ***This is a work in progress. API may change***

Lightweight AOP library, based on the Dagger 2 concept of static code generation and compile-time validation using Annotation Processing Tool (APT).

## What is it?

***dagger-aop*** is a library that generates a subclass of classes with annotated methods.

It should not be used directly by the end user, it has no method interceptors, it is a foundation to create method interceptors.

We use Java Service(Registered using [`InterceptorHandler`](https://github.com/0x3333/dagger-aop/blob/master/core/src/main/java/com/github/x3333/dagger/aop/InterceptorHandler.java)) to locate method interceptors.

This library has been created with a simple intent in mind, allow Dagger 2 based projects intercept methods to make them transactional(JPA). After some experiments, I generalized the code enabling it to create other interceptors. If you believe it should do something else, fell free to create an issue.

## How it works?

***dagger-aop*** generates a subclass of classes with annotated methods. This subclass override the annotated method and call the [`MethodInterceptor`](https://github.com/0x3333/dagger-aop/blob/master/core/src/main/java/com/github/x3333/dagger/aop/MethodInterceptor.java) code.

Multiple interceptors in a single method are supported. ***Curently the order of interceptors is undetermined***

Also, ***dagger-aop*** will generate a Dagger 2 module that will be used to bind your intercepted method.

The annotation is defined by the [`InterceptorHandler.annotation()`](https://github.com/0x3333/dagger-aop/blob/master/core/src/main/java/com/github/x3333/dagger/aop/InterceptorHandler.java#L44) method.

## Options

* `aop.disable.module.generation` - `boolean` - Disable Dagger 2 module generation.
* `aop.module.package` - `boolean` - Define the package in which the Dagger 2 module will be generated.
* 
You can pass using maven like this:

```xml
<build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <compilerArgs>
                        <!-- This is how to pass arguments to Dagger AOP Compiler -->
                        <arg>-Aaop.disable.module.generation=false</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

## Creating Interceptors

To create a Method Interceptor, you must have 3 things:

1. Annotation
2. MethodInterceptor
3. InterceptorHandler

This example can be found in `example` folder.

### Annotation

This is the annotation that your interceptor is binded to. Methods annotated with your annotation will be intercepted.

Your annotation can contain elements, that will be available to the interceptor itself in runtime. ***Annotations must have `@Retention(RUNTIME)` and `@Target(METHOD)`, otherwise they will not be registered!***


```java
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Log {
  String value();
}
```

### MethodInterceptor

This is the interceptor itself, it will be responsible to do the logic behind your interceptor and invoke the original method if needed. It must implement `<T> T invoke(MethodInvocation invocation) throws Throwable`.

```java
public class LogInterceptor implements MethodInterceptor {

  @Override
  public <T> T invoke(final MethodInvocation invocation) throws Throwable {
    Logger logger = LoggerFactory.getLogger(invocation.getInstance().getClass());
    
    // Method's Annotation 
    Log log = invocation.annotation(Log.class);
    
    logger.debug("{} - Method {} of {} invoked", 
      log.value(), // Annotation Value
      invocation.getMethod(),
      invocation.getInstance().toString()
    );
    
    return (T) invocation.proceed();
  }

}
```

### InterceptorHandler

InterceptorHandler binds the `Annotation` with the `MethodInterceptor`. It also can validate the element that is annotated and do some post processing in the intercepted classes.

InterceptorHandler must be registered in [Java Service](https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html) using `InterceptorHandler` class. We strongly suggest you to use [Google AutoService](https://github.com/google/auto/tree/master/service), it will generate services file automatically based on the `@AutoService` annotation. Without the service declared, ***dagger-aop*** is unable to find your interceptor.

```java
@AutoService(InterceptorHandler.class) // Google AutoService, if done manually, can be ommited
public class LogInterceptorHandler implements InterceptorHandler {

  @Override
  public Class<? extends Annotation> annotation() {
    return Log.class; // The Annotation
  }

  @Override
  public Class<LogInterceptor> methodInterceptorClass() {
    return LogInterceptor.class; // The MethodInterceptor itself
  }

  @Override
  public String validateMethod(final ExecutableElement methodElement) {
    if(!element.getSimpleName().toString().startsWith("log")) {
      return "Log methods must start with 'log'!";
    }
    return null;
  }

  @Override
  public void postProcess(final ProcessingEnvironment processingEnv, final Set<TypeElement> processedClasses) {
    // You can generate more classes, configuration files, etc.
  }

}
```

### Using Interceptors

To use the interceptor, besides adding the dependency, you have to annotate the methods you want to intercept and bind in dagger to use the generated class instead the original implementation.

Suppose you have a Interface and an Implementation of this interface. You would do something like this:

```java
public interface MyInterface {
  void doSomething();
}
```

```java
public class MyClass implements MyInterface {

  private final SomeDependency some;

  public MyClass(SomeDependency some) {
    this.some = some;  
  }

  @Override
  public void doSomething() {
    some.doWork();
  }

}
```

You will bind `MyInterface` to `MyClass` this in Dagger 2 like:

```java
@Module
public abstract class MyModule {
  @Binds
  abstract MyInterface providesMyInterface(MyClass impl);
}
```

Well, if we annotate the method like this:

```java
public class MyClass implements MyInterface {

  private final SomeDependency some;

  public MyClass(SomeDependency some) {
    this.some = some;  
  }

  @Log // !!! HERE !!!
  @Override
  public void doSomething() {
    some.doWork();
  }

}
```

We need to add 2 binds, one to the Interceptor and another to the newly created class, like this:

```java
@Module(includes = { InterceptorModule.class }) // Just add InterceptorModule in includes
public abstract class MyModule {
  @Binds
  abstract MyInterface providesMyInterface(MyClass impl);

  @Binds
  abstract MyClass providesMyClass(Interceptor_MyClass impl); // New generated class

  @Binds
  abstract LogInterceptor providesLogInterceptor(LogInterceptor impl); // Interceptor
}
```

## Other examples

As I said, there is a [***dagger-jpa***](https://github.com/0x3333/dagger-jpa) project which uses ***dagger-aop*** to make methods transactional using JPA. This is a better example on how to create an Interceptor.

## Usage

Currently it is not deployed to maven central, so you need to install on your local repo:

```bash
git clone git@github.com:0x3333/dagger-aop.git
cd dagger-aop
mvn clean install
```

License
-------

    Copyright (C) 2016 Tercio Gaudencio Filho

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

