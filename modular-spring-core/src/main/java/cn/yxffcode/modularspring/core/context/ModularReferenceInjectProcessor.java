package cn.yxffcode.modularspring.core.context;

import cn.yxffcode.modularspring.core.annotation.ModularReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import java.beans.PropertyDescriptor;

/**
 * @author gaohang on 7/8/17.
 */
public class ModularReferenceInjectProcessor extends AutowiredAnnotationBeanPostProcessor {
  public ModularReferenceInjectProcessor() {
    super();
    setAutowiredAnnotationType(ModularReference.class);
  }
}
