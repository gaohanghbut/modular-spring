package cn.yxffcode.modularspring.core;

import com.google.common.base.Strings;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

/**
 * @author gaohang on 6/24/17.
 */
public class ServiceBean implements ApplicationContextAware {
  private String ref;
  private String interfaceName;
  private String uniqueId;
  private ApplicationContext applicationContext;

  public ServiceBean(String beanRef, String interfaceName, String uniqueId) {
    this.interfaceName = interfaceName;
    this.uniqueId = Strings.nullToEmpty(uniqueId);
    this.ref = beanRef;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
    ServiceManager.registryService(this);
  }

  public String getRef() {
    return ref;
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
