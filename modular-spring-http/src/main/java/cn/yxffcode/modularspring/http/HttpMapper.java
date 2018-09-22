package cn.yxffcode.modularspring.http;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface HttpMapper {
  String value() default "";
}
