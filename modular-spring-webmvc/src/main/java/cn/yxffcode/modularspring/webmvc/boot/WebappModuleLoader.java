package cn.yxffcode.modularspring.webmvc.boot;

import cn.yxffcode.modularspring.boot.DefaultModuleLoader;
import cn.yxffcode.modularspring.boot.ModuleConfig;
import cn.yxffcode.modularspring.core.context.DefaultModuleApplicationContext;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import cn.yxffcode.modularspring.webmvc.context.DefaultWebModuleApplicationContext;
import com.google.common.base.Predicate;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * 支持web模块的加载
 *
 * @author gaohang on 7/9/17.
 */
public class WebappModuleLoader extends DefaultModuleLoader {
  private final ServletContext servletContext;
  private final Predicate<ModuleConfig> webModulePredicate;
  private final ServletConfig servletConfig;

  public WebappModuleLoader(ServletContext servletContext, ServletConfig servletConfig, Predicate<ModuleConfig> webModulePredicate) {
    this.servletContext = servletContext;
    this.webModulePredicate = webModulePredicate;
    this.servletConfig = servletConfig;
  }

  @Override
  protected ModuleApplicationContext createModuleApplicationContext(ModuleConfig moduleConfig, String[] configs) {
    if (!webModulePredicate.apply(moduleConfig)) {
      return new DefaultModuleApplicationContext(configs, false, null, moduleConfig.getModuleName());
    }
    DefaultWebModuleApplicationContext applicationContext = new DefaultWebModuleApplicationContext(
        configs, false, null, moduleConfig.getModuleName());
    applicationContext.setServletContext(servletContext);
    applicationContext.setServletConfig(servletConfig);
    return applicationContext;
  }
}
