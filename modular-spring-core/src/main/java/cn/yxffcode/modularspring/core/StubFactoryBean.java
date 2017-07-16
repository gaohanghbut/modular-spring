package cn.yxffcode.modularspring.core;

import com.google.common.reflect.Reflection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 可以指定Stub的InvocationHandler类名或者InvocationHandler在spring中的bean name，优先使用{@link #invocationHandlerRef}
 *
 * @author gaohang on 7/16/17.
 */
public class StubFactoryBean implements FactoryBean<Object>, ApplicationContextAware {
  private final Class<?> targetClass;

  /**
   * invocation handler class name
   */
  private String invocationHandler;

  /**
   * invocation handler ref name
   */
  private String invocationHandlerRef;

  private ApplicationContext applicationContext;

  public StubFactoryBean(Class<?> targetClass) {
    this.targetClass = targetClass;
  }

  @Override
  public Object getObject() throws Exception {
    if (StringUtils.isNotBlank(invocationHandlerRef)) {
      final InvocationHandler handler = applicationContext.getBean(invocationHandlerRef, InvocationHandler.class);
      return Reflection.newProxy(targetClass, handler);
    }
    if (StringUtils.isNotBlank(invocationHandler)) {
      final InvocationHandler handler = BeanUtils.instantiate((Class<InvocationHandler>) Class.forName(invocationHandler));
      applicationContext.getAutowireCapableBeanFactory()
          .autowireBeanProperties(handler, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
      return Reflection.newProxy(targetClass, handler);
    }

    return Reflection.newProxy(targetClass, DefaultStubInvocationHandler.INSTANCE);
  }

  @Override
  public Class<?> getObjectType() {
    return targetClass;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setInvocationHandler(String invocationHandler) {
    this.invocationHandler = invocationHandler;
  }

  public void setInvocationHandlerRef(String invocationHandlerRef) {
    this.invocationHandlerRef = invocationHandlerRef;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  private enum DefaultStubInvocationHandler implements InvocationHandler {
    INSTANCE;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return null;
    }
  }
}
