package cn.yxffcode.modularspring.webmvc;

import cn.yxffcode.modularspring.boot.Lifecycle;
import cn.yxffcode.modularspring.boot.ModuleConfig;
import cn.yxffcode.modularspring.boot.listener.ModuleLoadListener;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import cn.yxffcode.modularspring.webmvc.boot.WebappModuleLoader;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author gaohang on 7/9/17.
 */
public class ModularDispatcherServlet extends DispatcherServlet {

  private static final String WEB_APP_MODULE_PATTERN = "webModuleNamePrefix";

  private Lifecycle modularLifecycle;

  @Override
  protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {

    final WebModulePredicate webModulePredicate = new WebModulePredicate(getServletConfig().getInitParameter(WEB_APP_MODULE_PATTERN));
    final Lifecycle modularLifecycle = new Lifecycle(new WebappModuleLoader(getServletContext(), webModulePredicate));

    this.modularLifecycle = modularLifecycle;

    final GenericWebApplicationContext wac = new GenericWebApplicationContext(getServletContext());
    modularLifecycle.addModuleLoadListener(new WebModuleRegistryListener(webModulePredicate, wac));
    modularLifecycle.boot();

    //registry HandlerMapping
    registerHandlerMapping(wac);

    wac.refresh();
    return wac;
  }

  private void registerHandlerMapping(GenericWebApplicationContext wac) {
    final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
    wac.registerBeanDefinition(HANDLER_MAPPING_BEAN_NAME, rootBeanDefinition);
  }

  @Override
  public void destroy() {
    super.destroy();
    if (modularLifecycle != null) {
      modularLifecycle.destroy();
    }
  }

  private static class WebModulePredicate implements Predicate<ModuleConfig> {
    private final String webModuleNamePrefix;

    public WebModulePredicate(String webModuleNamePrefix) {
      this.webModuleNamePrefix = webModuleNamePrefix;
    }

    @Override
    public boolean apply(ModuleConfig moduleConfig) {
      return moduleConfig.getModuleName().startsWith(webModuleNamePrefix);
    }
  }

  private static class WebModuleRegistryListener implements ModuleLoadListener {
    private final WebModulePredicate webModulePredicate;
    private final GenericWebApplicationContext wac;

    public WebModuleRegistryListener(WebModulePredicate webModulePredicate, GenericWebApplicationContext wac) {
      this.webModulePredicate = webModulePredicate;
      this.wac = wac;
    }

    @Override
    public void beforeModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
    }

    @Override
    public void afterModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
      if (!webModulePredicate.apply(moduleConfig)) {
        return;
      }
      final String moduleName = moduleConfig.getModuleName();
      final int i = moduleName.lastIndexOf('.');
      final String contextBeanName = i < 0 ? moduleName : moduleName.substring(i + 1);
      if (StringUtils.isBlank(contextBeanName)) {
        throw new IllegalStateException("web模块的模块名不合法, 模块名:" + moduleName);
      }
      final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
      rootBeanDefinition.setBeanClass(ModuleApplicationContextBean.class);
      rootBeanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, applicationContext);
      wac.registerBeanDefinition(contextBeanName, rootBeanDefinition);
    }
  }
}
