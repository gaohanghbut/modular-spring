package cn.yxffcode.modularspring.core;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author gaohang on 5/28/17.
 */
public class ModuleApplicationContext extends ClassPathXmlApplicationContext {
  private final String moduleName;

  public ModuleApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent, String moduleName) throws BeansException {
    super(configLocations, false, parent);
    this.moduleName = moduleName;
    if (refresh) {
      refresh();
    }
  }
}
