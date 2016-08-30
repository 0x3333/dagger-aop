package test;

import dagger.Binds;
import dagger.Module;

/**
 * This class is the default Dagger module for Intercepted Methods.
 */
@Module
public abstract class InterceptorModule {
  @Binds
  abstract WithoutConstructor providesWithoutConstructor(final Interceptor_WithoutConstructor impl);
}
