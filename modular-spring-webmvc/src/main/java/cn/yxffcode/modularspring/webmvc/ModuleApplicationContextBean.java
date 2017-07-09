package cn.yxffcode.modularspring.webmvc;

import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author gaohang on 7/9/17.
 */
public class ModuleApplicationContextBean implements FactoryBean<ModuleApplicationContext> {
  private final ModuleApplicationContext moduleApplicationContext;

  public ModuleApplicationContextBean(ModuleApplicationContext moduleApplicationContext) {
    this.moduleApplicationContext = moduleApplicationContext;
  }

  @Override
  public ModuleApplicationContext getObject() throws Exception {
    return moduleApplicationContext;
  }

  @Override
  public Class<?> getObjectType() {
    return ModuleApplicationContext.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
