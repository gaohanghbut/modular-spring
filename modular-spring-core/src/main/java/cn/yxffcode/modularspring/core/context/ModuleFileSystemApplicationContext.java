package cn.yxffcode.modularspring.core.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

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
