package cn.yxffcode.modularspring.core.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * @author gaohang on 5/28/17.
 */
public class ModuleFileSystemApplicationContext extends AbstractModuleApplicationContext {

  public ModuleFileSystemApplicationContext(String[] configLocations, boolean refresh,
                                            ApplicationContext parent, String moduleName) throws BeansException {
    super(parent, moduleName);
    setConfigLocations(configLocations);
    if (refresh) {
      refresh();
    }
  }

  @Override
  protected Resource getResourceByPath(String path) {
    if (path != null && path.startsWith("/")) {
      path = path.substring(1);
    }
    return new FileSystemResource(path);
  }

}
