package cn.yxffcode.modularspring.webmvc.boot;

import cn.yxffcode.modularspring.boot.DefaultModuleLoader;
import cn.yxffcode.modularspring.boot.ModuleConfig;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import cn.yxffcode.modularspring.core.context.ModuleJarEntryXmlApplicationContext;
import cn.yxffcode.modularspring.webmvc.context.WebModuleFileSystemApplicationContext;
import cn.yxffcode.modularspring.webmvc.context.WebModuleJarEntryXmlApplicationContext;
import com.google.common.base.Predicate;

import javax.servlet.ServletContext;

/**
 * 支持web模块的加载
 *
 * @author gaohang on 7/9/17.
 */
public class WebappModuleLoader extends DefaultModuleLoader {
  private final ServletContext servletContext;
  private final Predicate<ModuleConfig> webModulePredicate;

  public WebappModuleLoader(ServletContext servletContext, Predicate<ModuleConfig> webModulePredicate) {
    this.servletContext = servletContext;
    this.webModulePredicate = webModulePredicate;
  }

  @Override
  protected ModuleApplicationContext createModuleApplicationContext(ModuleConfig moduleConfig, String[] configs, String moduleName) {
    if (!webModulePredicate.apply(moduleConfig)) {
      return super.createModuleApplicationContext(moduleConfig, configs, moduleName);
    }
    ModuleApplicationContext applicationContext;
    if (moduleConfig.isFromFile()) {
      applicationContext = new WebModuleFileSystemApplicationContext(configs, false, null, moduleName, servletContext);
    } else {
      applicationContext = new WebModuleJarEntryXmlApplicationContext(configs, false, null, moduleName, servletContext);
    }
    return applicationContext;
  }
}
