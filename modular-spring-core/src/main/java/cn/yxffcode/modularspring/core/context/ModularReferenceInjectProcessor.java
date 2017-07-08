package cn.yxffcode.modularspring.core.context;

import cn.yxffcode.modularspring.core.annotation.ModularReference;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;

/**
 * @author gaohang on 7/8/17.
 */
public class ModularReferenceInjectProcessor extends AutowiredAnnotationBeanPostProcessor {
  public ModularReferenceInjectProcessor() {
    super();
    setAutowiredAnnotationType(ModularReference.class);
  }
}
