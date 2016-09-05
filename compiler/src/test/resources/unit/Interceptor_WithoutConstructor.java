package test;

import com.github.x3333.dagger.aop.AbstractMethodInvocation;
import com.github.x3333.dagger.aop.test.Interceptor;
import com.github.x3333.dagger.aop.test.Interceptor2;
import java.lang.Integer;
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

@Generated(
    value = "com.github.x3333.dagger.aop.internal.InterceptorProcessor",
    comments = "https://github.com/0x3333/dagger-aop"
)
public final class Interceptor_WithoutConstructor extends WithoutConstructor {
  private static final Method doSomeWorkReturnCache$;

  private static final List<Annotation> doSomeWorkReturnAnnotationsCache$;

  private static final Method doSomeWorkReturnPrimitiveCache$;

  private static final List<Annotation> doSomeWorkReturnPrimitiveAnnotationsCache$;

  private static final Method doSomeWorkNoReturnCache$;

  private static final List<Annotation> doSomeWorkNoReturnAnnotationsCache$;

  private static final Method doSomeWorkNoReturnThrowsCache$;

  private static final List<Annotation> doSomeWorkNoReturnThrowsAnnotationsCache$;

  private static final Method doSomeWorkMultipleAndThrowsCache$;

  private static final List<Annotation> doSomeWorkMultipleAndThrowsAnnotationsCache$;

  static {
    try {
      doSomeWorkReturnCache$ = Interceptor_WithoutConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkReturn");
      doSomeWorkReturnAnnotationsCache$ = Arrays.asList(doSomeWorkReturnCache$.getAnnotations());
      doSomeWorkReturnPrimitiveCache$ = Interceptor_WithoutConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkReturnPrimitive", String.class);
      doSomeWorkReturnPrimitiveAnnotationsCache$ = Arrays.asList(doSomeWorkReturnPrimitiveCache$.getAnnotations());
      doSomeWorkNoReturnCache$ = Interceptor_WithoutConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkNoReturn", String.class);
      doSomeWorkNoReturnAnnotationsCache$ = Arrays.asList(doSomeWorkNoReturnCache$.getAnnotations());
      doSomeWorkNoReturnThrowsCache$ = Interceptor_WithoutConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkNoReturnThrows");
      doSomeWorkNoReturnThrowsAnnotationsCache$ = Arrays.asList(doSomeWorkNoReturnThrowsCache$.getAnnotations());
      doSomeWorkMultipleAndThrowsCache$ = Interceptor_WithoutConstructor.class.getSuperclass().getDeclaredMethod("doSomeWorkMultipleAndThrows");
      doSomeWorkMultipleAndThrowsAnnotationsCache$ = Arrays.asList(doSomeWorkMultipleAndThrowsCache$.getAnnotations());
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private final Interceptor $interceptorValidAnnotation;

  private final Interceptor2 $interceptorValidAnnotation2;

  @Inject
  public Interceptor_WithoutConstructor(final Interceptor $interceptorValidAnnotation, final Interceptor2 $interceptorValidAnnotation2) {
    super();
    this.$interceptorValidAnnotation = $interceptorValidAnnotation;
    this.$interceptorValidAnnotation2 = $interceptorValidAnnotation2;
  }

  @Override
  public String doSomeWorkReturn() {
    final Object[] arguments = new Object[] {  };
    try {
      return (String) $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
          Interceptor_WithoutConstructor.this,
          Interceptor_WithoutConstructor.doSomeWorkReturnCache$,
          arguments,
          Interceptor_WithoutConstructor.doSomeWorkReturnAnnotationsCache$) {
        @Override
        public Object proceed() throws Throwable {
          return Interceptor_WithoutConstructor.super.doSomeWorkReturn();
        }
      });
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int doSomeWorkReturnPrimitive(String param1) {
    final Object[] arguments = new Object[] { param1 };
    try {
      return (Integer) $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
          Interceptor_WithoutConstructor.this,
          Interceptor_WithoutConstructor.doSomeWorkReturnPrimitiveCache$,
          arguments,
          Interceptor_WithoutConstructor.doSomeWorkReturnPrimitiveAnnotationsCache$) {
        @Override
        public Object proceed() throws Throwable {
          return Interceptor_WithoutConstructor.super.doSomeWorkReturnPrimitive(param1);
        }
      });
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doSomeWorkNoReturn(String param1) {
    final Object[] arguments = new Object[] { param1 };
    try {
      $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
          Interceptor_WithoutConstructor.this,
          Interceptor_WithoutConstructor.doSomeWorkNoReturnCache$,
          arguments,
          Interceptor_WithoutConstructor.doSomeWorkNoReturnAnnotationsCache$) {
        @Override
        protected void noReturnProceed() throws Throwable {
          Interceptor_WithoutConstructor.super.doSomeWorkNoReturn(param1);
        }
      });
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doSomeWorkNoReturnThrows() throws WithoutConstructor.MyException {
    final Object[] arguments = new Object[] {  };
    try {
      $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
          Interceptor_WithoutConstructor.this,
          Interceptor_WithoutConstructor.doSomeWorkNoReturnThrowsCache$,
          arguments,
          Interceptor_WithoutConstructor.doSomeWorkNoReturnThrowsAnnotationsCache$) {
        @Override
        protected void noReturnProceed() throws Throwable {
          Interceptor_WithoutConstructor.super.doSomeWorkNoReturnThrows();
        }
      });
    } catch (WithoutConstructor.MyException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doSomeWorkMultipleAndThrows() throws WithoutConstructor.MyException {
    final Object[] arguments = new Object[] {  };
    try {
      $interceptorValidAnnotation2.invoke(new AbstractMethodInvocation(
          Interceptor_WithoutConstructor.this,
          Interceptor_WithoutConstructor.doSomeWorkMultipleAndThrowsCache$,
          arguments,
          Interceptor_WithoutConstructor.doSomeWorkMultipleAndThrowsAnnotationsCache$) {
        @Override
        protected void noReturnProceed() throws Throwable {
          $interceptorValidAnnotation.invoke(new AbstractMethodInvocation(
              Interceptor_WithoutConstructor.this,
              Interceptor_WithoutConstructor.doSomeWorkMultipleAndThrowsCache$,
              arguments,
              Interceptor_WithoutConstructor.doSomeWorkMultipleAndThrowsAnnotationsCache$) {
            @Override
            protected void noReturnProceed() throws Throwable {
              Interceptor_WithoutConstructor.super.doSomeWorkMultipleAndThrows();
            }
          });
        }
      });
    } catch (WithoutConstructor.MyException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
