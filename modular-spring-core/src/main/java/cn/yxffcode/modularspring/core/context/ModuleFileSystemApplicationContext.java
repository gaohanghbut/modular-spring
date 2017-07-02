package cn.yxffcode.modularspring.core.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

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
  public String getModuleName() {
    return moduleName;
  }
}
