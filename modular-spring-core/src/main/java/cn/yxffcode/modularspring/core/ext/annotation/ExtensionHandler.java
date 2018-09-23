package cn.yxffcode.modularspring.core.ext.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在类上，用于说明此类可用于处理扩展点
 *
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionHandler {
  /**
   * @return extension handler name， bean name for default value
   */
  String value() default "";
}
