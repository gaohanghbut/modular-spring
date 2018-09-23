package cn.yxffcode.modularspring.core.ext.annotation;

import cn.yxffcode.modularspring.core.ext.utils.ExtensionPointUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author gaohang
 */
public class ExtensionPointAnnotationRegistryProcessor implements ImportBeanDefinitionRegistrar {
  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
    for (String beanName : beanDefinitionRegistry.getBeanDefinitionNames()) {
      AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) beanDefinitionRegistry.getBeanDefinition(beanName);

      Class<?> beanType = getBeanType(beanDefinition);
      if (beanType == null) {
        continue;
      }
      ExtensionPoint extensionPoint = beanType.getAnnotation(ExtensionPoint.class);
      if (extensionPoint == null) {
        continue;
      }

      ExtensionPointUtils.registryExtensionPoint(beanDefinitionRegistry, extensionPoint.value(), beanName);

    }
  }

  //C:\Users\gaoha\Projects\moduler-spring-test\core-service\target\classes\
  //C:/Users/gaoha/Projects/moduler-spring-test/core-service/target/classes/cn/yxffcode/modularspring/test/core/AnoTestExtensionPoint.class

  private Class<?> getBeanType(AbstractBeanDefinition beanDefinition) {
    Class<?> beanType;
    String beanClassName = beanDefinition.getBeanClassName();
    try {
      if (StringUtils.isNotBlank(beanClassName)) {
        beanType = Class.forName(beanClassName);
      } else {
        beanType = beanDefinition.getBeanClass();
      }
    } catch (ClassNotFoundException e) {
      beanType = null;
    }
    return beanType;
  }

}
