package cn.yxffcode.modularspring.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 发布服务
 *
 * @author gaohang on 7/8/17.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ModularService {

  /**
   * @return bean的名字
   */
  String value() default "";

  Class<?> interfaceClass() default Object.class;

  String uniqueId() default "";
}
