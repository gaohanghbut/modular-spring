package cn.yxffcode.modularspring.core.ext.annotation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * @author gaohang
 */
public class ExtAnnotationRegistryProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        for (String beanName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) beanDefinitionRegistry.getBeanDefinition(beanName);

            Class<?> beanType = getBeanType(beanDefinition);
            if (beanType == null) {
                continue;
            }
            ExtensionHandler extensionHandler = beanType.getAnnotation(ExtensionHandler.class);
            if (extensionHandler == null) {
                continue;
            }
            //注册bean
        }
    }

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

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
