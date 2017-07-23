package cn.yxffcode.modularspring.webmvc.context;

import cn.yxffcode.modularspring.core.context.DefaultModuleApplicationContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.ui.context.support.UiApplicationContextUtils;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextAwareProcessor;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author gaohang on 7/23/17.
 */
public class DefaultWebModuleApplicationContext extends DefaultModuleApplicationContext implements ThemeSource {

  private ServletContext servletContext;

  private ServletConfig servletConfig;

  private String namespace;

  private ThemeSource themeSource;

  public DefaultWebModuleApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent, String moduleName) {
    super(configLocations, refresh, parent, moduleName);
    setDisplayName(moduleName + " WebApplicationContext");
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  public ServletContext getServletContext() {
    return this.servletContext;
  }

  public void setServletConfig(ServletConfig servletConfig) {
    this.servletConfig = servletConfig;
    if (servletConfig != null && this.servletContext == null) {
      setServletContext(servletConfig.getServletContext());
    }
  }

  public ServletConfig getServletConfig() {
    return this.servletConfig;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
    if (namespace != null) {
      setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
    }
  }

  public String getNamespace() {
    return this.namespace;
  }

  @Override
  public String[] getConfigLocations() {
    return super.getConfigLocations();
  }

  @Override
  public String getApplicationName() {
    return (this.servletContext != null ? this.servletContext.getContextPath() : "");
  }

  /**
   * Create and return a new {@link StandardServletEnvironment}. Subclasses may override
   * in order to configure the environment or specialize the environment type returned.
   */
  @Override
  protected ConfigurableEnvironment createEnvironment() {
    return new StandardServletEnvironment();
  }

  /**
   * Register request/session scopes, a {@link ServletContextAwareProcessor}, etc.
   */
  @Override
  protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    beanFactory.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext, this.servletConfig));
    beanFactory.ignoreDependencyInterface(ServletContextAware.class);
    beanFactory.ignoreDependencyInterface(ServletConfigAware.class);

    WebApplicationContextUtils.registerWebApplicationScopes(beanFactory, this.servletContext);
    WebApplicationContextUtils.registerEnvironmentBeans(beanFactory, this.servletContext, this.servletConfig);
  }

  /**
   * This implementation supports file paths beneath the root of the ServletContext.
   *
   * @see ServletContextResource
   */
  @Override
  protected Resource getResourceByPath(String path) {
    return new ServletContextResource(this.servletContext, path);
  }

  /**
   * This implementation supports pattern matching in unexpanded WARs too.
   *
   * @see ServletContextResourcePatternResolver
   */
  @Override
  protected ResourcePatternResolver getResourcePatternResolver() {
    return new ServletContextResourcePatternResolver(this);
  }

  /**
   * Initialize the theme capability.
   */
  @Override
  protected void onRefresh() {
    this.themeSource = UiApplicationContextUtils.initThemeSource(this);
  }

  /**
   * {@inheritDoc}
   * <p>Replace {@code Servlet}-related property sources.
   */
  @Override
  protected void initPropertySources() {
    ConfigurableEnvironment env = getEnvironment();
    if (env instanceof ConfigurableWebEnvironment) {
      ((ConfigurableWebEnvironment) env).initPropertySources(this.servletContext, this.servletConfig);
    }
  }

  @Override
  public Theme getTheme(String themeName) {
    return this.themeSource.getTheme(themeName);
  }
}
