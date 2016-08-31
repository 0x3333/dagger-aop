package test;

import com.github.x3333.dagger.aop.AbstractMethodInvocation;
import com.github.x3333.dagger.aop.test.Interceptor;
import com.github.x3333.dagger.aop.test.Interceptor2;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.Throwable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Generated;
import javax.inject.Inject;
import javax.inject.Named;

@Generated(
    value = "com.github.x3333.dagger.aop.internal.InterceptorProcessor",
    comments = "https://github.com/0x3333/dagger-aop"
)
public final class Interceptor_WithConstructor extends WithConstructor {
  private static final Method doSomeWorkReturnCache$;

  private static final List<Annotation> doSomeWorkReturnAnnotationsCache$;

  private static final Method doSomeWorkNoReturnCache$;

  private static final List<Annotation> doSomeWorkNoReturnAnnotationsCache$;

  private static final Method doSomeWorkNoReturnThrowsCache$;

  private static final List<Annotation> doSomeWorkNoReturnThrowsAnnotationsCache$;

  private static final Method doSomeWorkMultipleAndThrowsCache$;

  private static final List<Annotation> doSomeWorkMultipleAndThrowsAnnotationsCache$;

  static {
    try {
      doSomeWorkReturnCache$ = Interceptor_WithConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkReturn", String.class);
      doSomeWorkReturnAnnotationsCache$ = Arrays.asList(doSomeWorkReturnCache$.getAnnotations());
      doSomeWorkNoReturnCache$ = Interceptor_WithConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkNoReturn");
      doSomeWorkNoReturnAnnotationsCache$ = Arrays.asList(doSomeWorkNoReturnCache$.getAnnotations());
      doSomeWorkNoReturnThrowsCache$ = Interceptor_WithConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkNoReturnThrows");
      doSomeWorkNoReturnThrowsAnnotationsCache$ = Arrays.asList(doSomeWorkNoReturnThrowsCache$.getAnnotations());
      doSomeWorkMultipleAndThrowsCache$ = Interceptor_WithConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkMultipleAndThrows");
      doSomeWorkMultipleAndThrowsAnnotationsCache$ = Arrays.asList(doSomeWorkMultipleAndThrowsCache$.getAnnotations());
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private final Interceptor $interceptorValidAnnotation;

  private final Interceptor2 $interceptorValidAnnotation2;

  @Inject
  public Interceptor_WithConstructor(@Named("myDepAnnotation") final String someDep, final Interceptor $interceptorValidAnnotation, final Interceptor2 $interceptorValidAnnotation2) {
    super(someDep);
    this.$interceptorValidAnnotation = $interceptorValidAnnotation;
    this.$interceptorValidAnnotation2 = $interceptorValidAnnotation2;
  }

  @Override
  public String doSomeWorkReturn(String param1) {
    final Object[] arguments = new Object[] { param1 };
    try {
      return $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
          Interceptor_WithConstructor.this,
          Interceptor_WithConstructor.doSomeWorkReturnCache$,
          arguments,
          Interceptor_WithConstructor.doSomeWorkReturnAnnotationsCache$) {
        @Override
        public String proceed() throws Throwable {
          return Interceptor_WithConstructor.super.doSomeWorkReturn(param1);
        }
      });
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doSomeWorkNoReturn() {
    final Object[] arguments = new Object[] {  };
    try {
      $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
          Interceptor_WithConstructor.this,
          Interceptor_WithConstructor.doSomeWorkNoReturnCache$,
          arguments,
          Interceptor_WithConstructor.doSomeWorkNoReturnAnnotationsCache$) {
        @Override
        protected void noReturnProceed() throws Throwable {
          Interceptor_WithConstructor.super.doSomeWorkNoReturn();
        }
      });
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doSomeWorkNoReturnThrows() throws WithConstructor.MyException {
    final Object[] arguments = new Object[] {  };
    try {
      $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
          Interceptor_WithConstructor.this,
          Interceptor_WithConstructor.doSomeWorkNoReturnThrowsCache$,
          arguments,
          Interceptor_WithConstructor.doSomeWorkNoReturnThrowsAnnotationsCache$) {
        @Override
        protected void noReturnProceed() throws Throwable {
          Interceptor_WithConstructor.super.doSomeWorkNoReturnThrows();
        }
      });
    } catch (WithConstructor.MyException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doSomeWorkMultipleAndThrows() throws WithConstructor.MyException {
    final Object[] arguments = new Object[] {  };
    try {
      $interceptorValidAnnotation2.invoke(new AbstractMethodInvocation(
          Interceptor_WithConstructor.this,
          Interceptor_WithConstructor.doSomeWorkMultipleAndThrowsCache$,
          arguments,
          Interceptor_WithConstructor.doSomeWorkMultipleAndThrowsAnnotationsCache$) {
        @Override
        protected void noReturnProceed() throws Throwable {
          $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
              Interceptor_WithConstructor.this,
              Interceptor_WithConstructor.doSomeWorkMultipleAndThrowsCache$,
              arguments,
              Interceptor_WithConstructor.doSomeWorkMultipleAndThrowsAnnotationsCache$) {
            @Override
            protected void noReturnProceed() throws Throwable {
              Interceptor_WithConstructor.super.doSomeWorkMultipleAndThrows();
            }
          });
        }
      });
    } catch (WithConstructor.MyException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
