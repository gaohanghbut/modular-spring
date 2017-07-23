package cn.yxffcode.modularspring.webmvc.context;

import cn.yxffcode.modularspring.webmvc.view.ModuleResourceViewResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author gaohang on 7/23/17.
 */
public class ViewResolverRegistry implements BeanDefinitionRegistryPostProcessor {
  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    if (registry.containsBeanDefinition(DispatcherServlet.VIEW_RESOLVER_BEAN_NAME)) {
      return;
    }
    final RootBeanDefinition def = new RootBeanDefinition(ModuleResourceViewResolver.class);
    registry.registerBeanDefinition(DispatcherServlet.VIEW_RESOLVER_BEAN_NAME, def);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
  }
}
