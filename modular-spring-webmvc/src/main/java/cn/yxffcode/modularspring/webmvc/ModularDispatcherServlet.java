package cn.yxffcode.modularspring.webmvc;

import cn.yxffcode.modularspring.boot.Lifecycle;
import cn.yxffcode.modularspring.boot.ModuleConfig;
import cn.yxffcode.modularspring.boot.listener.ModuleLoadListener;
import cn.yxffcode.modularspring.boot.utils.ModuleUtils;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import cn.yxffcode.modularspring.webmvc.boot.WebappModuleLoader;
import cn.yxffcode.modularspring.webmvc.request.ModuleRequestMappingHandlerMapping;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * @author gaohang on 7/9/17.
 */
public class ModularDispatcherServlet extends DispatcherServlet {

  private static final String WEB_APP_MODULE_PATTERN = "webModuleNamePrefix";

  /**
   * 用于配置webmvc基础组件的applicationContext的配置文件路径,可以没有
   */
  private static final String COMMON_COMPONENT_PATH = "commonApplicationContext";

  private Lifecycle modularLifecycle;

  @Override
  protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {

    final WebModulePredicate webModulePredicate = new WebModulePredicate(getServletConfig().getInitParameter(WEB_APP_MODULE_PATTERN));
    final Lifecycle modularLifecycle = new Lifecycle(new WebappModuleLoader(getServletContext(), webModulePredicate));

    this.modularLifecycle = modularLifecycle;

    //初始化基础组件容器
    final WebApplicationContext commonApplicationContext = createCommonComponentApplicationContext();

    final GenericWebApplicationContext wac = new GenericWebApplicationContext(getServletContext());
    modularLifecycle.addModuleLoadListener(new WebModuleRegistryListener(webModulePredicate, wac, commonApplicationContext));
    modularLifecycle.boot();

    registerHandlerMapping(wac);

    registerHandlerAdapter(wac);

    wac.refresh();
    return wac;
  }

  private WebApplicationContext createCommonComponentApplicationContext() {
    final String commonSpringConfig = getServletConfig().getInitParameter(COMMON_COMPONENT_PATH);
    WebApplicationContext commonApplicationContext = null;
    if (StringUtils.isNotBlank(commonSpringConfig)) {
      final XmlWebApplicationContext ctx = new XmlWebApplicationContext();
      ctx.setServletContext(getServletContext());
      ctx.setConfigLocation(commonSpringConfig);
      ctx.refresh();
      commonApplicationContext = ctx;
    }
    return commonApplicationContext;
  }

  private void registerHandlerMapping(GenericWebApplicationContext wac) {
    final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
    rootBeanDefinition.setBeanClass(ModuleRequestMappingHandlerMapping.class);
    wac.registerBeanDefinition(HANDLER_MAPPING_BEAN_NAME, rootBeanDefinition);
  }

  private void registerHandlerAdapter(GenericWebApplicationContext wac) {
    final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
    rootBeanDefinition.setBeanClass(RequestMappingHandlerAdapter.class);
    wac.registerBeanDefinition(HANDLER_ADAPTER_BEAN_NAME, rootBeanDefinition);
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
    private final WebApplicationContext commonApplicationContext;

    public WebModuleRegistryListener(WebModulePredicate webModulePredicate, GenericWebApplicationContext wac,
                                     WebApplicationContext commonApplicationContext) {
      this.commonApplicationContext = commonApplicationContext;
      this.webModulePredicate = webModulePredicate;
      this.wac = wac;
    }

    @Override
    public void beforeModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
      if (!webModulePredicate.apply(moduleConfig)) {
        return;
      }
      if (commonApplicationContext != null) {
        applicationContext.setParent(commonApplicationContext);
      }
    }

    @Override
    public void afterModuleLoad(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
      if (!webModulePredicate.apply(moduleConfig)) {
        return;
      }
      final String contextBeanName = ModuleUtils.getSimpleModuleName(moduleConfig);
      if (StringUtils.isBlank(contextBeanName)) {
        throw new IllegalStateException("web模块的模块名不合法, 模块名:" + moduleConfig.getModuleName());
      }
      final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
      rootBeanDefinition.setBeanClass(ModuleApplicationContextBean.class);
      rootBeanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, applicationContext);
      wac.registerBeanDefinition(contextBeanName, rootBeanDefinition);
    }
  }

}
