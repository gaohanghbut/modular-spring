package cn.yxffcode.modularspring.core.ext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * @author gaohang
 */
public class ExtensionHandlerBean implements ApplicationContextAware {
  private ApplicationContext applicationContext;

  private final String extensionName;
  private final String ref;
  private final List<ListenerMethod> listenerMethods;

  public ExtensionHandlerBean(String extensionName, String ref, List<ListenerMethod> listenerMethods) {
    this.extensionName = extensionName;
    this.ref = ref;
    this.listenerMethods = listenerMethods;
  }

  public String getExtensionName() {
    return extensionName;
  }

  public String getRef() {
    return ref;
  }

  public List<ListenerMethod> getListenerMethods() {
    return listenerMethods;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public static final class ListenerMethod {
    private final String methodName;
    private final Class<?> extensionType;

    public ListenerMethod(String methodName, Class<?> extensionType) {
      this.methodName = methodName;
      this.extensionType = extensionType;
    }

    public String getMethodName() {
      return methodName;
    }

    public Class<?> getExtensionType() {
      return extensionType;
    }
  }
}
