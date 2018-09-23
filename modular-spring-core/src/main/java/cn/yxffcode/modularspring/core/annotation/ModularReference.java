package cn.yxffcode.modularspring.core.annotation;

import java.lang.annotation.*;

/**
 * 引用其它模块中的服务
 *
 * @author gaohang on 7/8/17.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ModularReference {
  String uniqueId() default "";
}
