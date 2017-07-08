package cn.yxffcode.modularspring.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
  Class<?> interfaceClass() default Object.class;

  String uniqueId() default "";
}
