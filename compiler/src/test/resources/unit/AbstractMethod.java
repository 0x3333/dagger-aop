package test;

import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation;

public abstract class AbstractMethod {

  @ValidAnnotation
  public void doSomeWorkNoReturn() {}

  public abstract void invalidMethod() {}

}
