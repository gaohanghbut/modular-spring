package cn.yxffcode.modularspring.core.ext;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author gaohang
 */
public class DefaultExtensionHandlerInjector implements ExtensionHandlerInjector {

  private final ApplicationContext applicationContext;

  public DefaultExtensionHandlerInjector(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public void inject(ExtensionHandlerBean extensionHandlerBean) {
    if (extensionHandlerBean.getListenerMethods() == null || extensionHandlerBean.getListenerMethods().isEmpty()) {
      return;
    }
    Collection<ExtensionPointBean> extensionPoints = ExtensionHolder.getExtensionPoint(extensionHandlerBean.getExtensionName());
    if (extensionPoints == null || extensionPoints.isEmpty()) {
      return;
    }
    Object bean = applicationContext.getBean(extensionHandlerBean.getRef());
    try {
      for (ExtensionHandlerBean.ListenerMethod listenerMethod : extensionHandlerBean.getListenerMethods()) {
        Method method = getMethod(bean.getClass(), listenerMethod);
        Class<?> extensionType = listenerMethod.getExtensionType();
        for (ExtensionPointBean extensionPoint : extensionPoints) {
          Object extension = extensionPoint.getBean();
          if (extensionType.isAssignableFrom(extension.getClass())) {
            //invoke listener method
            method.invoke(bean, extension);
          }
        }
      }
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }

  }

  private Method getMethod(Class<?> type, ExtensionHandlerBean.ListenerMethod listenerMethod) throws NoSuchMethodException {
    Method[] methods = type.getMethods();
    for (Method method : methods) {
      if (method.getName().equals(listenerMethod.getMethodName())) {
        return method;
      }
    }
    throw new NoSuchMethodException(listenerMethod.getMethodName() + " is not exists in " + type.getName());
  }

}
