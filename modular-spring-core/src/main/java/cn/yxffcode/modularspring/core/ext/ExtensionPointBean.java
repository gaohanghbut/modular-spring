package cn.yxffcode.modularspring.core.ext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author gaohang on 7/9/17.
 */
public class ExtensionPointBean implements ApplicationContextAware {
  private final String extensionName;
  private final String beanName;

  private ApplicationContext applicationContext;

  public ExtensionPointBean(String extensionName, String beanName) {
    this.extensionName = extensionName;
    this.beanName = beanName;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
    ExtensionHolder.registryExtensionPoint(this);
  }

  public String getExtensionName() {
    return extensionName;
  }

  public String getBeanName() {
    return beanName;
  }

  public Object getBean() {
    return applicationContext.getBean(beanName);
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
