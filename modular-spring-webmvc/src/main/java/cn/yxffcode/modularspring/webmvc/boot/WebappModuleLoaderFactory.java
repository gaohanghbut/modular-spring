package cn.yxffcode.modularspring.webmvc.boot;

import cn.yxffcode.modularspring.boot.ModuleConfig;
import cn.yxffcode.modularspring.boot.ModuleLoader;
import cn.yxffcode.modularspring.boot.ModuleLoaderFactory;
import com.google.common.base.Predicate;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.lang.reflect.Constructor;

/**
 * @author gaohang on 2/16/18.
 */
public class WebappModuleLoaderFactory implements ModuleLoaderFactory {

  private final ServletContext servletContext;
  private final ServletConfig servletConfig;
  private final Predicate<ModuleConfig> webModulePredicate;

  public WebappModuleLoaderFactory(ServletContext servletContext, ServletConfig servletConfig, Predicate<ModuleConfig> webModulePredicate) {
    this.servletContext = servletContext;
    this.servletConfig = servletConfig;
    this.webModulePredicate = webModulePredicate;
  }

  @Override
  public ModuleLoader newModuleLoader(ClassLoader classLoader) {
    try {
      final Class<?> loader = classLoader.loadClass("cn.yxffcode.modularspring.webmvc.boot.WebappModuleLoader");
      final Constructor<?> constr = loader.getDeclaredConstructor(ServletContext.class, ServletConfig.class, Predicate.class);
      return (ModuleLoader) constr.newInstance(servletContext, servletConfig, webModulePredicate);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
