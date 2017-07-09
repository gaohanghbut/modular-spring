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
public class ModuleJarEntryXmlApplicationContext extends AbstractModuleApplicationContext {

  public ModuleJarEntryXmlApplicationContext(String[] configLocations, boolean refresh,
                                             ApplicationContext parent, String moduleName) throws BeansException {

    super(parent, moduleName);
    setConfigLocations(configLocations);
    if (refresh) {
      refresh();
    }
  }

  @Override
  protected Resource getResourceByPath(String path) {
    return new JarEntryResource(path);
  }

}
