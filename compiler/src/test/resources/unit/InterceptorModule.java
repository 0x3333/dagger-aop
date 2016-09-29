package test;

import dagger.Binds;
import dagger.Module;
import javax.annotation.Generated;

/**
 * This class is the default Dagger module for Intercepted Methods.
 */
@Generated(
    value = "com.github.x3333.dagger.aop.internal.InterceptorProcessorStep",
    comments = "https://github.com/0x3333/dagger-aop"
)
@Module
public abstract class InterceptorModule {
  @Binds
  abstract WithoutConstructor providesWithoutConstructor(final Interceptor_WithoutConstructor impl);
}
