package cn.yxffcode.modularspring.springboot;

import cn.yxffcode.modularspring.webmvc.ModularDispatcherServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebMvcProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.DispatcherServlet;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author gaohang on 7/29/17.
 */
public abstract class ModularServletInitializer extends SpringBootServletInitializer {

  private static final String[] DEFAULT_URL_PATTERN = {"/"};

  @Autowired
  private WebMvcProperties webMvcProperties;

  @Bean
  public ServletRegistrationBean dispatcherServletRegistration(DispatcherServlet dispatcherServlet) {
    DispatcherServlet servlet = dispatcherServlet;
    if (!(dispatcherServlet instanceof ModularDispatcherServlet)) {
      final ModularDispatcherServlet modularServlet = new ModularDispatcherServlet();
      modularServlet.setDispatchOptionsRequest(
          this.webMvcProperties.isDispatchOptionsRequest());
      modularServlet.setDispatchTraceRequest(
          this.webMvcProperties.isDispatchTraceRequest());
      modularServlet.setThrowExceptionIfNoHandlerFound(
          this.webMvcProperties.isThrowExceptionIfNoHandlerFound());

      final ModularDispatcherConfig modularDispatcherConfig = AnnotationUtils.findAnnotation(getClass(), ModularDispatcherConfig.class);
      checkState(modularDispatcherConfig != null, "必须配置@ModularDispatcherConfig");

      modularServlet.setWebModuleNamePrefix(modularDispatcherConfig.webModuleNamePrefix());
      modularServlet.setCommonApplicationContext(modularDispatcherConfig.commonApplicationContext());

      servlet = modularServlet;
    }
    final UrlPattern urlPattern = AnnotationUtils.findAnnotation(getClass(), UrlPattern.class);
    final ServletRegistrationBean registration = urlPattern == null
        ? new ServletRegistrationBean(servlet, DEFAULT_URL_PATTERN)
        : new ServletRegistrationBean(servlet, urlPattern.value());
    registration.setName("dispatcherServlet");

    registration.setLoadOnStartup(0);

    return registration;
  }

}
