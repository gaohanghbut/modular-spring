package cn.yxffcode.modularspring.webmvc.request;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.HandlerMethodSelector;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author gaohang on 7/9/17.
 */
public class ModuleRequestMappingHandlerMapping extends RequestMappingHandlerMapping implements ApplicationContextAware {
  private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

  private boolean detectHandlerMethodsInAncestorContexts;

  private ApplicationContext currentApplicationContext;

  @Override
  protected final RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
    throw new UnsupportedOperationException("仅支持通过模块配置controller");
  }

  protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType, String webModuleSimpleName) {

    final RequestMappingInfo requestMappingInfo = createRequestMappingInfo(new ModuleRequestMapping(webModuleSimpleName), null);
    return requestMappingInfo.combine(super.getMappingForMethod(method, handlerType));
  }

  protected final void initHandlerMethods() {
    if (logger.isDebugEnabled()) {
      logger.debug("Looking for request mappings in application context: " + getApplicationContext());
    }

    final String[] beanNames = getBeanNames(getApplicationContext());

    for (String beanName : beanNames) {
      if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX) &&
          isHandler(getApplicationContext().getType(beanName))) {
        detectHandlerMethods(beanName);
      } else {
        final Class<?> type = getApplicationContext().getType(beanName);
        if (!ApplicationContext.class.isAssignableFrom(type)) {
          continue;
        }
        final ApplicationContext ctx = getApplicationContext().getBean(beanName, ApplicationContext.class);
        currentApplicationContext = ctx;
        final String[] moduleBeanNames = getBeanNames(ctx);
        for (String moduleBeanName : moduleBeanNames) {
          if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX) &&
              isHandler(ctx.getType(moduleBeanName))) {
            detectHandlerMethods(moduleBeanName, beanName);
          }
        }
        currentApplicationContext = null;
      }
    }
    handlerMethodsInitialized(getHandlerMethods());
  }

  protected void detectHandlerMethods(final Object handler, String webModuleSimpleName) {
    Class<?> handlerType =
        (handler instanceof String ? currentApplicationContext.getType((String) handler) : handler.getClass());

    // Avoid repeated calls to getMappingForMethod which would rebuild RequestMappingInfo instances
    final Map<Method, RequestMappingInfo> mappings = new IdentityHashMap<Method, RequestMappingInfo>();
    final Class<?> userType = ClassUtils.getUserClass(handlerType);

    Set<Method> methods = HandlerMethodSelector.selectMethods(userType, new ReflectionUtils.MethodFilter() {
      @Override
      public boolean matches(Method method) {
        RequestMappingInfo mapping = getMappingForMethod(method, userType, webModuleSimpleName);
        if (mapping != null) {
          mappings.put(method, mapping);
          return true;
        } else {
          return false;
        }
      }
    });

    for (Method method : methods) {
      registerHandlerMethod(handler, method, mappings.get(method));
    }
  }

  @Override
  protected final HandlerMethod createHandlerMethod(Object handler, Method method) {
    HandlerMethod handlerMethod;
    if (handler instanceof String) {
      String beanName = (String) handler;
      handlerMethod = new HandlerMethod(beanName,
          currentApplicationContext.getAutowireCapableBeanFactory(), method);
    } else {
      handlerMethod = new HandlerMethod(handler, method);
    }
    return handlerMethod;
  }

  @Override
  public void setDetectHandlerMethodsInAncestorContexts(boolean detectHandlerMethodsInAncestorContexts) {
    this.detectHandlerMethodsInAncestorContexts = detectHandlerMethodsInAncestorContexts;
    super.setDetectHandlerMethodsInAncestorContexts(detectHandlerMethodsInAncestorContexts);
  }

  private String[] getBeanNames(ApplicationContext applicationContext) {
    return (this.detectHandlerMethodsInAncestorContexts ?
        BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, Object.class) :
        applicationContext.getBeanNamesForType(Object.class));
  }

  @Override
  protected void detectMappedInterceptors(List<MappedInterceptor> mappedInterceptors) {
    final Collection<ApplicationContext> values = BeanFactoryUtils.beansOfTypeIncludingAncestors(
        getApplicationContext(), ApplicationContext.class, true, false).values();
    for (ApplicationContext value : values) {
      mappedInterceptors.addAll(
          BeanFactoryUtils.beansOfTypeIncludingAncestors(
              value, MappedInterceptor.class, true, false).values());
    }
    //支持commonApplicationContext中配置拦截器
    mappedInterceptors.addAll(
        BeanFactoryUtils.beansOfTypeIncludingAncestors(
            getApplicationContext(), MappedInterceptor.class, true, false).values());
  }


}
