package test;

import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation;

public class NotAbstract {

  @ValidAnnotation
  public void doSomeWorkNoReturn() {}

}
