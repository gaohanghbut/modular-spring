package cn.yxffcode.modularspring.core.context;

import cn.yxffcode.modularspring.core.ServiceDeclarationException;
import cn.yxffcode.modularspring.core.annotation.ModularReference;
import cn.yxffcode.modularspring.core.annotation.ModularService;
import cn.yxffcode.modularspring.core.config.ModularBeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.*;

import java.lang.reflect.Field;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 7/8/17.
 */
public class ModularBeanDefinitionRegistry implements BeanDefinitionRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(ModularBeanDefinitionRegistry.class);

  private final BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

  private final BeanDefinitionRegistry delegate;

  public ModularBeanDefinitionRegistry(BeanDefinitionRegistry delegate) {
    this.delegate = checkNotNull(delegate);
    final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(ModularReferenceInjectProcessor.class);
    delegate.registerBeanDefinition(ModularBeanDefinitionRegistry.class.getName(), rootBeanDefinition);
  }

  @Override
  public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {
    delegate.registerBeanDefinition(beanName, beanDefinition);
    //check if this bean is a modular service
    Class<?> beanClass = null;
    try {
      beanClass = getServiceBeanClass(beanDefinition);
    } catch (Exception ignore) {
      LOGGER.warn("ignore error when register service bean by annotation:{}", ignore.getMessage());
    }

    if (beanClass == null) {
      return;
    }

    registerServiceBeanIfNeed(beanName, beanClass);
    //reference
    registerServiceReferenceIfNeed(beanClass);
  }

  private void registerServiceBeanIfNeed(String beanName, Class<?> beanClass) {
    final ModularService modularService = beanClass.getDeclaredAnnotation(ModularService.class);
    if (modularService != null) {
      Class<?> serviceInterface = modularService.interfaceClass();
      if (serviceInterface == null || serviceInterface == Object.class) {
        final Class<?>[] interfaces = beanClass.getInterfaces();
        if (ArrayUtils.isEmpty(interfaces)) {
          throw new BeanDefinitionStoreException("服务定义出错",
              new ServiceDeclarationException("modular service定义异常,bean 没有实现接口,bean的类型:" + serviceInterface.getName()));
        }
        serviceInterface = interfaces[0];
      }
      final String uniqueId = modularService.uniqueId();
      final RootBeanDefinition rootBeanDefinition = ModularBeanUtils.buildServiceBean(beanName, serviceInterface.getName(), uniqueId);
      delegate.registerBeanDefinition(beanNameGenerator.generateBeanName(rootBeanDefinition, delegate), rootBeanDefinition);
    }
  }

  private void registerServiceReferenceIfNeed(Class<?> beanClass) {
    final Field[] declaredFields = beanClass.getDeclaredFields();
    if (ArrayUtils.isEmpty(declaredFields)) {
      return;
    }

    for (Field field : declaredFields) {
      final ModularReference modularReference = field.getDeclaredAnnotation(ModularReference.class);
      if (modularReference == null) {
        continue;
      }
      final Class<?> type = field.getType();
      final String referenceBeanName = getReferenceBeanName(type);
      if (delegate.containsBeanDefinition(referenceBeanName)) {
        continue;
      }
      final RootBeanDefinition rootBeanDefinition = ModularBeanUtils.buildReferenceBean(type, modularReference.uniqueId());
      delegate.registerBeanDefinition(referenceBeanName, rootBeanDefinition);
    }
  }

  private String getReferenceBeanName(Class<?> type) {
    return type.getSimpleName() + "ModularReference#";
  }

  private Class<?> getServiceBeanClass(BeanDefinition beanDefinition) {
    final String beanClassName = beanDefinition.getBeanClassName();
    try {
      return Class.forName(beanClassName);
    } catch (ClassNotFoundException e) {
      if (beanDefinition instanceof AbstractBeanDefinition) {
        return ((AbstractBeanDefinition) beanDefinition).getBeanClass();
      }
      throw new BeanDefinitionStoreException("类不存在:" + beanClassName);
    }
  }

  @Override
  public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
    delegate.removeBeanDefinition(beanName);
  }

  @Override
  public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
    return delegate.getBeanDefinition(beanName);
  }

  @Override
  public boolean containsBeanDefinition(String beanName) {
    return delegate.containsBeanDefinition(beanName);
  }

  @Override
  public String[] getBeanDefinitionNames() {
    return delegate.getBeanDefinitionNames();
  }

  @Override
  public int getBeanDefinitionCount() {
    return delegate.getBeanDefinitionCount();
  }

  @Override
  public boolean isBeanNameInUse(String beanName) {
    return delegate.isBeanNameInUse(beanName);
  }

  @Override
  public void registerAlias(String name, String alias) {
    delegate.registerAlias(name, alias);
  }

  @Override
  public void removeAlias(String alias) {
    delegate.removeAlias(alias);
  }

  @Override
  public boolean isAlias(String beanName) {
    return delegate.isAlias(beanName);
  }

  @Override
  public String[] getAliases(String name) {
    return delegate.getAliases(name);
  }
}
