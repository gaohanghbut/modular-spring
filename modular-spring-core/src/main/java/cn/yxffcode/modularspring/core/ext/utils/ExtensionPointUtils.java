package cn.yxffcode.modularspring.core.ext.utils;

import cn.yxffcode.modularspring.core.ext.ExtensionHandlerBean;
import cn.yxffcode.modularspring.core.ext.ExtensionPointBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.List;

/**
 * @author gaohang
 */
public abstract class ExtensionPointUtils {
  private static final BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

  private ExtensionPointUtils() {
  }

  public static void registryExtensionPoint(BeanDefinitionRegistry registry, String extensionName, String ref) {
    final RootBeanDefinition bean = new RootBeanDefinition();
    bean.setBeanClass(ExtensionPointBean.class);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(0, extensionName);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(1, ref);

    registry.registerBeanDefinition(beanNameGenerator.generateBeanName(bean, registry), bean);
  }

  public static void registryExtensionHandler(BeanDefinitionRegistry registry, String extensionName, String ref, List<ExtensionHandlerBean.ListenerMethod> listenerMethods) {
    final RootBeanDefinition bean = new RootBeanDefinition();
    bean.setBeanClass(ExtensionHandlerBean.class);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(0, extensionName);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(1, ref);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(2, listenerMethods);

    registry.registerBeanDefinition(beanNameGenerator.generateBeanName(bean, registry), bean);
  }
}
