package cn.yxffcode.modularspring.core.ext.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author gaohang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ExtensionHandlerAnnotationRegistryProcessor.class)
public @interface EnableExtensionHandler {
}
