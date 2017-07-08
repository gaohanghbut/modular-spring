package cn.yxffcode.modularspring.core.context;

import cn.yxffcode.modularspring.core.io.JarEntryResource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * 从jar文件中读取xml配置创建ApplicationContext
 */
public class ModuleJarEntryXmlApplicationContext extends AbstractXmlApplicationContext implements ModuleApplicationContext {
  private final String moduleName;

  public ModuleJarEntryXmlApplicationContext(String[] configLocations, boolean refresh,
                                             ApplicationContext parent, String moduleName) throws BeansException {

    super(parent);
    setConfigLocations(configLocations);
    if (refresh) {
      refresh();
    }
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
  protected Resource getResourceByPath(String path) {
    return new JarEntryResource(path);
  }

  @Override
  public String getModuleName() {
    return moduleName;
  }
}
