package cn.yxffcode.modularspring.core;

import com.google.common.base.Strings;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author gaohang on 7/8/17.
 */
public final class ModularBeanUtils {
  private ModularBeanUtils() {
  }

  public static RootBeanDefinition buildServiceBean(String ref, String anInterface, String uniqueId) {
    final RootBeanDefinition bean = new RootBeanDefinition();
    bean.setBeanClass(ServiceBean.class);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(0, new RuntimeBeanNameReference(ref));
    bean.getConstructorArgumentValues().addIndexedArgumentValue(1, anInterface);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(2, Strings.nullToEmpty(uniqueId));
    return bean;
  }

  public static RootBeanDefinition buildReferenceBean(Class<?> anInterface, String uniqueId) {
    final RootBeanDefinition bean = new RootBeanDefinition();
    bean.setBeanClass(ServiceReference.class);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(0, anInterface);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(1, uniqueId);
    return bean;
  }
}
