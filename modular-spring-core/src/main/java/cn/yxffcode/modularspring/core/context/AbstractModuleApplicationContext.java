package cn.yxffcode.modularspring.core.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * @author gaohang on 7/9/17.
 */
abstract class AbstractModuleApplicationContext extends AbstractXmlApplicationContext implements ModuleApplicationContext {
  protected final String moduleName;

  public AbstractModuleApplicationContext(ApplicationContext parent, String moduleName) {
    super(parent);
    this.moduleName = moduleName;
  }

  @Override
  protected final void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
    // Create a new XmlBeanDefinitionReader for the given BeanFactory.
    final XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(new ModularBeanDefinitionRegistry(beanFactory));

    beanDefinitionReader.setEnvironment(this.getEnvironment());
    beanDefinitionReader.setResourceLoader(this);
    beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

    initBeanDefinitionReader(beanDefinitionReader);
    loadBeanDefinitions(beanDefinitionReader);
  }

  @Override
  protected abstract Resource getResourceByPath(String path);

  @Override
  public String getModuleName() {
    return moduleName;
  }
}
