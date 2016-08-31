package test;

import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation;
import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation2;

public abstract class WithoutConstructor {

  public static class MyException extends Exception {
  }

  @ValidAnnotation
  public String doSomeWorkReturn() {
    return null;
  }

  @ValidAnnotation
  public void doSomeWorkNoReturn(String param1) {}

  @ValidAnnotation
  public void doSomeWorkNoReturnThrows() throws MyException {}

  @ValidAnnotation
  @ValidAnnotation2
  public void doSomeWorkMultipleAndThrows() throws MyException {}

}
