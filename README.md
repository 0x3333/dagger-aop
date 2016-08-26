# Dagger2 AOP

### ***This is a work in progress. API is not defined yet.*** ###

Usage
-----

dagger-aop is a basic AOP implementation based on the concept of static code generation and compile-time method interceptor using Annotation Processing Tool (APT).

By itself, it has no method interceptors, but it is fully extensible using Java Service Handlers implementing a simple interface.

Basically, it creates a subclass of the intercepted class, with the target methods replaced by a custom code, which will call the `MethodInterceptor`. It can be nested, allowing multiple interceptions in a single method.

This library has been created with a simple intent in mind, allow Dagger 2 based projects intercept methods to make them transactional. After some experiments, I generalized the code allowing to create other interceptors. If you believe it should do something else, fell free to create an issue.

Example
-------

Imagine that you want to log some information everytime a method is called. You could just add the logger call to the method, like:

```java
public abstract class SomeClass {

  public void doSomeWork() {
    logger.debug("doSomeWork has been called!");
  }

}
```

Well, imagine that you have to do this for several methods. It is verbose and cumbersome. So, we can create an `MethodInterceptor` that will do this based on a single annotation.

This is your class that will be intercepted.

```java
public abstract class SomeClass {

  @Log
  public void doSomeWork() {
    // doSomeWork implementation
  }

}
```

The Annotation that will be used to mark methods for the interception. Remember to set Retention to `RUNTIME`, interceptors need this annotation on runtime. A annotation can contain values which the interceptor needs in runtime.
```java
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Log { }
```

The MethodInterceptor implementation itself, this is that do the "hard" work.
```java
public class LogInterceptor implements MethodInterceptor {

  public <T> T invoke(final MethodInvocation invocation) throws Throwable {
    LoggerFactory.getLogger(invocation.getInstance().getClass()).debug("Method {} of {} invoked",
        invocation.getMethod(),
        invocation.getInstance().getClass().getName() + "@" + Integer.toHexString(invocation.getInstance().hashCode()));
    return (T) invocation.proceed();
  }

}
```

Next, the `InterceptorHandler`, which is used by the `InterceptorGenerator` to provide informations about your interception, also do some validations to the target element.
I highly suggest you to use [Google AutoService](https://github.com/google/auto/tree/master/service), it will generate services file automatically based on the `@AutoService` annotation, which is mandatory, without the service declared, the `InterceptorGenerator` is unable to find your MethodInterceptor.
```java
@AutoService(InterceptorHandler.class)
public class LogInterceptorHandler implements InterceptorHandler {

  public Class<? extends Annotation> annotation() {
    return Log.class; // The Annotation
  }

  public Class<LogInterceptor> methodInterceptorClass() {
    return LogInterceptor.class; // The MethodInterceptor itself
  }

}
```

We are all set! Now, a new class will be generated, Interceptor_SomeClass, this newly created class will create a constructor, or modify a existent one with its new dependency, LogInterceptor. You have to use this class, bind it in your Dagger Module.

More documentation as I progress.

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

