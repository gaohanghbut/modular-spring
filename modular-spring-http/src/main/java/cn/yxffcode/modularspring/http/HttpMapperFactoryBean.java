package cn.yxffcode.modularspring.http;

import cn.yxffcode.modularspring.http.cfg.Configuration;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author gaohang on 8/12/17.
 */
public class HttpMapperFactoryBean implements FactoryBean<Object> {
  private final Class<?> interfaceType;
  private final Configuration configuration;

  public HttpMapperFactoryBean(Class<?> interfaceType, Configuration configuration) {
    this.interfaceType = interfaceType;
    this.configuration = configuration;
  }

  @Override
  public Object getObject() throws Exception {
    return configuration.newMapper(interfaceType);
  }

  @Override
  public Class<?> getObjectType() {
    return interfaceType;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
