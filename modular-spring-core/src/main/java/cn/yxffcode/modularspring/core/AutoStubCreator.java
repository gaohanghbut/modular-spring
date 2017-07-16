package cn.yxffcode.modularspring.core;

import com.google.common.base.Throwables;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;

/**
 * @author gaohang on 7/16/17.
 */
public class AutoStubCreator implements BeanFactoryPostProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(AutoStubCreator.class);

  private void reDefineBean(AbstractBeanDefinition beanDefinition, Class<?> beanType) {
    if (!ServiceReference.class.isAssignableFrom(beanType)) {
      return;
    }
    beanDefinition.setBeanClass(ServiceReferenceStub.class);
    beanDefinition.setBeanClassName(ServiceReferenceStub.class.getCanonicalName());

    //获取服务接口类型
    final ConstructorArgumentValues.ValueHolder valueHolder = beanDefinition.getConstructorArgumentValues().getIndexedArgumentValue(0, Class.class);

    final PropertyValue stub = new PropertyValue("stub",
        StubBeanNameUtils.getStubNameForInterface((Class<?>) valueHolder.getValue()));
    //stub不是必须的
    beanDefinition.getPropertyValues().addPropertyValue(stub);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    final String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
    if (ArrayUtils.isEmpty(beanDefinitionNames)) {
      return;
    }
    for (String beanDefinitionName : beanDefinitionNames) {
      final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);

      final String beanClassName = beanDefinition.getBeanClassName();

      try {
        final Class<?> type = Class.forName(beanClassName);
        reDefineBean((AbstractBeanDefinition) beanDefinition, type);
      } catch (ClassNotFoundException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  /**
   * 创建代理，自动为未实现的服务打桩
   */
  public static final class ServiceReferenceStub extends ServiceReference implements ApplicationContextAware {

    private String stub;

    private ApplicationContext applicationContext;

    public ServiceReferenceStub(Class<?> targetClass, String uniqueId) {
      super(targetClass, uniqueId);
    }

    @Override
    public Object getObject() throws Exception {
      return Reflection.newProxy(getObjectType(), new AbstractInvocationHandler() {
        private Object delegate;

        private void initDelegate() {
          final ServiceBean service = ServiceManager.getService(getObjectType().getName(), getUniqueId());
          if (service != null) {
            final ApplicationContext ctx = service.getApplicationContext();
            this.delegate = ctx.getBean(service.getRef());
            return;
          }
          LOGGER.warn("没有找到" + getObjectType().getCanonicalName() + "引用的服务，尝试使用auto-stub。");
          if (!applicationContext.containsBean(stub)) {
            throw new ServiceLocatingException("服务 " + getObjectType() + " 没有找到,请检查是否是模块依赖不正确");
          }
          this.delegate = applicationContext.getBean(stub);
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

    public void setStub(String stub) {
      this.stub = stub;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      this.applicationContext = applicationContext;
    }
  }
}
