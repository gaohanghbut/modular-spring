package cn.yxffcode.modularspring.webmvc;

import cn.yxffcode.modularspring.boot.ApplicationManager;
import cn.yxffcode.modularspring.boot.ModuleConfig;
import cn.yxffcode.modularspring.boot.listener.ModuleLoadListener;
import cn.yxffcode.modularspring.boot.utils.ModuleUtils;
import cn.yxffcode.modularspring.core.ModularSpringConfiguration;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import cn.yxffcode.modularspring.webmvc.boot.WebappModuleLoaderFactory;
import cn.yxffcode.modularspring.webmvc.request.ModuleRequestMappingHandlerMapping;
import cn.yxffcode.modularspring.webmvc.view.ModuleResourceViewResolver;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
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

  private static final String MODULAR_SPRING_CONFIG_PATH = "modularSpringConfig";

  private ApplicationManager modularApplicationManager;

  private String webModuleNamePrefix;
  private String commonApplicationContext;

  @Override
  protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {

    final WebModulePredicate webModulePredicate = new WebModulePredicate(
        StringUtils.isBlank(webModuleNamePrefix)
            ? getServletConfig().getInitParameter(WEB_APP_MODULE_PATTERN)
            : webModuleNamePrefix
    );

    final ModularSpringConfiguration modularSpringConfiguration = new ModularSpringConfiguration();

    //todo:临时代码
    modularSpringConfiguration.setPluginPath("/tmp/modularspring/plugins");

    final ApplicationManager modularApplicationManager = new ApplicationManager(modularSpringConfiguration, new WebappModuleLoaderFactory(getServletContext(), getServletConfig(), webModulePredicate));

    this.modularApplicationManager = modularApplicationManager;

    //初始化基础组件容器
    final WebApplicationContext commonApplicationContext = createCommonComponentApplicationContext(parent);

    final GenericWebApplicationContext wac = new GenericWebApplicationContext(getServletContext());
    if (commonApplicationContext != null) {
      wac.setParent(commonApplicationContext);
    }
    modularApplicationManager.addModuleLoadListener(new WebModuleRegistryListener(webModulePredicate, wac, commonApplicationContext));
    modularApplicationManager.boot();

    registerHandlerMapping(wac);

    registerHandlerAdapter(wac);

    //register view resolver
    registerViewResolver(wac);

    wac.refresh();
    return wac;
  }

  private void registerViewResolver(GenericWebApplicationContext wac) {
    if (!wac.containsBean(VIEW_RESOLVER_BEAN_NAME)) {
      final RootBeanDefinition def = new RootBeanDefinition(ModuleResourceViewResolver.class);
      wac.registerBeanDefinition(VIEW_RESOLVER_BEAN_NAME, def);
    }
  }

  private WebApplicationContext createCommonComponentApplicationContext(ApplicationContext parent) {
    final String commonSpringConfig = StringUtils.isBlank(commonApplicationContext)
        ? getServletConfig().getInitParameter(COMMON_COMPONENT_PATH)
        : commonApplicationContext;
    WebApplicationContext commonApplicationContext = null;
    if (StringUtils.isNotBlank(commonSpringConfig)) {
      final XmlWebApplicationContext ctx = new XmlWebApplicationContext();
      ctx.setServletContext(getServletContext());
      ctx.setConfigLocation(commonSpringConfig);
      ctx.setParent(parent);
      ctx.refresh();
      commonApplicationContext = ctx;
    }
    return commonApplicationContext;
  }

  private void registerHandlerMapping(GenericWebApplicationContext wac) {
    final ApplicationContext parent = wac.getParent();
    if (parent != null && parent.containsBeanDefinition(HANDLER_MAPPING_BEAN_NAME)) {
      return;
    }
    if (parent != null) {
      try {
        parent.getBean(ModuleRequestMappingHandlerMapping.class);
        return;
      } catch (BeansException e) {
        //ignore
      }
    }
    final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
    rootBeanDefinition.setBeanClass(ModuleRequestMappingHandlerMapping.class);
    wac.registerBeanDefinition(HANDLER_MAPPING_BEAN_NAME, rootBeanDefinition);
  }

  private void registerHandlerAdapter(GenericWebApplicationContext wac) {
    final ApplicationContext parent = wac.getParent();
    if (parent != null && parent.containsBeanDefinition(HANDLER_ADAPTER_BEAN_NAME)) {
      return;
    }
    if (parent != null) {
      try {
        parent.getBean(RequestMappingHandlerAdapter.class);
        return;
      } catch (BeansException e) {
        //ignore
      }
    }
    final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
    rootBeanDefinition.setBeanClass(RequestMappingHandlerAdapter.class);
    wac.registerBeanDefinition(HANDLER_ADAPTER_BEAN_NAME, rootBeanDefinition);
  }

  @Override
  public void destroy() {
    super.destroy();
    if (modularApplicationManager != null) {
      modularApplicationManager.destroy();
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

  public String getWebModuleNamePrefix() {
    return webModuleNamePrefix;
  }

  public void setWebModuleNamePrefix(String webModuleNamePrefix) {
    this.webModuleNamePrefix = webModuleNamePrefix;
  }

  public String getCommonApplicationContext() {
    return commonApplicationContext;
  }

  public void setCommonApplicationContext(String commonApplicationContext) {
    this.commonApplicationContext = commonApplicationContext;
  }
}
