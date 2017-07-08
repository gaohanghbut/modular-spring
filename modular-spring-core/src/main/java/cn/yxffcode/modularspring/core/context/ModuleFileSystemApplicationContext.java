package cn.yxffcode.modularspring.core.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.IOException;

/**
 * @author gaohang on 5/28/17.
 */
public class ModuleFileSystemApplicationContext extends FileSystemXmlApplicationContext implements ModuleApplicationContext {
  private final String moduleName;

  public ModuleFileSystemApplicationContext(String[] configLocations, boolean refresh,
                                            ApplicationContext parent, String moduleName) throws BeansException {
    super(configLocations, false, parent);
    this.moduleName = moduleName;
    if (refresh) {
      refresh();
    }
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
  public String getModuleName() {
    return moduleName;
  }
}
