package test;

import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation;
import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation2;

import javax.inject.Named;

public abstract class WithConstructor {

  public static class MyException extends Exception {
  }

  public WithConstructor(@Named("myDepAnnotation") final String someDep) {}
  
  @ValidAnnotation
  public String doSomeWorkReturn(String param1) {
    return null;
  }

  @ValidAnnotation
  public void doSomeWorkNoReturn() {}
  
  @ValidAnnotation
  public void doSomeWorkNoReturnThrows() throws MyException {}

  @ValidAnnotation
  @ValidAnnotation2
  public void doSomeWorkMultipleAndThrows() throws MyException {}

}
