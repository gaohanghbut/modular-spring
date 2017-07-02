package cn.yxffcode.modularspring.core.context;

import cn.yxffcode.modularspring.core.io.JarEntryResource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

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
  protected Resource getResourceByPath(String path) {
    return new JarEntryResource(path);
  }

  @Override
  public String getModuleName() {
    return moduleName;
  }
}
