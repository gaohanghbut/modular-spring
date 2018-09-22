package cn.yxffcode.modularspring.http;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author gaohang
 */
public class ModularSpringHttpRegistrar implements ImportBeanDefinitionRegistrar {

  private static final BeanNameGenerator DEFAULT_BEAN_NAME_GENERATOR = new DefaultBeanNameGenerator();

  @Override
  public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
    AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableModularSpringHttp.class.getName()));
    String[] basePackages = annoAttrs.getStringArray("basePackages");
    boolean createModularService = annoAttrs.getBoolean("createModularService");

    // HttpMapperAutoConfigurer
    RootBeanDefinition def = new RootBeanDefinition();
    def.setBeanClass(HttpMapperAutoConfigurer.class);
    def.setAttribute("basePackages", basePackages);
    def.setAttribute("annotation", HttpMapper.class);
    def.setAttribute("createModularService", createModularService);

    beanDefinitionRegistry.registerBeanDefinition(DEFAULT_BEAN_NAME_GENERATOR.generateBeanName(def, beanDefinitionRegistry), def);
  }
}
