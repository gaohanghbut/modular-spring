package cn.yxffcode.modularspring.boot.listener;

import cn.yxffcode.modularspring.boot.ModuleConfig;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;

/**
 * @author gaohang on 7/9/17.
 */
public interface ModuleLoadListener {

  /**
   * 在调用{@link ModuleApplicationContext#refresh()}前调用
   */
  void beforeModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext);

  /**
   * 在调用{@link ModuleApplicationContext#refresh()}后调用
   */
  void afterModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext);
}
