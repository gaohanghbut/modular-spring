package cn.yxffcode.modularspring.core.context;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author gaohang on 7/2/17.
 */
public interface ModuleApplicationContext extends ConfigurableApplicationContext {
  String getModuleName();
}
