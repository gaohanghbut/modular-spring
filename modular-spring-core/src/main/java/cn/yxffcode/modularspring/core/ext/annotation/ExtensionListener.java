package cn.yxffcode.modularspring.core.ext.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记在方法上，用于标示此方法可接受某个类型的扩展对象
 *
 * @author gaohang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtensionListener {
    /**
     * 表示接收的扩展点类型，如果是Object，则根据参数类型做匹配
     */
    Class<?> value() default Object.class;
}
