package cn.yxffcode.modularspring.core;

import com.google.common.base.Strings;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

/**
 * @author gaohang on 6/24/17.
 */
public final class ServiceReference implements FactoryBean<Object> {
  private final Class<?> targetClass;
  private final String uniqueId;

  public ServiceReference(Class<?> targetClass, String uniqueId) {
    this.targetClass = targetClass;
    this.uniqueId = Strings.nullToEmpty(uniqueId);
  }

  @Override
  public Object getObject() throws Exception {
    return Reflection.newProxy(targetClass, new AbstractInvocationHandler() {
      private Object delegate;

      private void initDelegate() {
        final ServiceBean service = ServiceManager.getService(targetClass.getName(), uniqueId);
        if (service == null) {
          throw new ServiceLocatingException("服务 " + targetClass + " 没有找到,请检查是否是模块依赖不正确");
        }
        final ApplicationContext ctx = service.getApplicationContext();
        this.delegate = ctx.getBean(service.getRef());
      }

      @Override
      protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        if (delegate == null) {
          synchronized (this) {
            if (delegate == null) {
              initDelegate();
            }
          }
        }
        return method.invoke(delegate, args);
      }
    });
  }

  @Override
  public Class<?> getObjectType() {
    return targetClass;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
