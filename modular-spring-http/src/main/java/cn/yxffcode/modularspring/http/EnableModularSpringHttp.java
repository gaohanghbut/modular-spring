package cn.yxffcode.modularspring.http;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ModularSpringHttpRegistrar.class})
public @interface EnableModularSpringHttp {

  /**
   * @return base packages
   */
  String[] basePackages() default "";

  boolean createModularService() default false;
}
