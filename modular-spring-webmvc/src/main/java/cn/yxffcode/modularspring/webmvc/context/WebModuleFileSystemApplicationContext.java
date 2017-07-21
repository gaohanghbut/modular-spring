package cn.yxffcode.modularspring.webmvc.context;

import cn.yxffcode.modularspring.core.context.ModuleFileSystemApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author gaohang on 7/9/17.
 */
public class WebModuleFileSystemApplicationContext extends ModuleFileSystemApplicationContext implements WebModuleApplicationContext {
  private final ServletContext servletContext;

  public WebModuleFileSystemApplicationContext(String[] configLocations, boolean refresh,
                                               ApplicationContext parent, String moduleName,
                                               ServletContext servletContext) throws BeansException {
    super(configLocations, refresh, parent, moduleName);
    this.servletContext = servletContext;
  }

  @Override
  protected void preProcessBeforeRefresh() {
    super.preProcessBeforeRefresh();
    addBeanFactoryPostProcessor(new MappedInterceptorNormalizer());
  }

  @Override
  public ServletContext getServletContext() {
    return servletContext;
  }
}
