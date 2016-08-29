package test;

import com.github.x3333.dagger.aop.test.annotation.ValidAnnotation;

import javax.inject.Named;

public abstract class MultipleConstructors {

  public MultipleConstructors() {}

  public MultipleConstructors(@Named("myDepAnnotation") final String someDep) {}

  @ValidAnnotation
  public void doSomeWorkNoReturn() {}

}
