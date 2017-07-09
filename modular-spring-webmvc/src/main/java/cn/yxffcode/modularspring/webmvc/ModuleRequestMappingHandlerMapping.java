package cn.yxffcode.modularspring.webmvc;

import cn.yxffcode.modularspring.webmvc.context.WebModuleApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author gaohang on 7/9/17.
 */
public class ModuleRequestMappingHandlerMapping extends RequestMappingHandlerMapping implements ApplicationContextAware {

  @Override
  protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
    final ApplicationContext applicationContext = getApplicationContext();
    final Map<String, WebModuleApplicationContext> webModuleApplicationContexts = applicationContext.getBeansOfType(WebModuleApplicationContext.class);

    RequestMappingInfo requestMappingInfo = null;
    BeansException ex = null;
    for (Map.Entry<String, WebModuleApplicationContext> en : webModuleApplicationContexts.entrySet()) {
      try {
        final Object bean = en.getValue().getBean(handlerType);
        //如果找到了bean,则表示在此applicationContext表示的模块中
        requestMappingInfo = createRequestMappingInfo(new ModuleRequestMapping(en.getKey()), null);
        break;
      } catch (BeansException e) {
        ex = e;
      }
    }
    if (ex != null) {
      throw ex;
    }
    if (requestMappingInfo == null) {
      throw new WebModuleLocateException("找不到模块, controller: " + handlerType.getCanonicalName());
    }
    return requestMappingInfo.combine(super.getMappingForMethod(method, handlerType));
  }

}
