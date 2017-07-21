package cn.yxffcode.modularspring.webmvc.context;

import cn.yxffcode.modularspring.boot.utils.ModuleLoadContextHolder;
import cn.yxffcode.modularspring.boot.utils.ModuleUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.web.servlet.handler.MappedInterceptor;

import java.util.Objects;

/**
 * @author gaohang on 7/22/17.
 */
public class MappedInterceptorNormalizer implements BeanFactoryPostProcessor {
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    final String simpleModuleName = ModuleUtils.getSimpleModuleName(ModuleLoadContextHolder.getLoadingModuleConfig());

    final String[] beanNames = beanFactory.getBeanDefinitionNames();
    for (String beanName : beanNames) {
      final BeanDefinition def = beanFactory.getBeanDefinition(beanName);
      final String beanClassName = def.getBeanClassName();
      if (!Objects.equals(beanClassName, MappedInterceptor.class.getCanonicalName())) {
        continue;
      }
      final ConstructorArgumentValues constructorArgumentValues = def.getConstructorArgumentValues();

      final ConstructorArgumentValues.ValueHolder urlPattern = constructorArgumentValues.getIndexedArgumentValue(0, ManagedList.class);
      normalizePatterns(simpleModuleName, (ManagedList) urlPattern.getValue());

      final ConstructorArgumentValues.ValueHolder urlExcludePattern = constructorArgumentValues.getIndexedArgumentValue(1, ManagedList.class);
      normalizePatterns(simpleModuleName, (ManagedList) urlExcludePattern.getValue());
    }
  }

  private void normalizePatterns(String simpleModuleName, ManagedList<String> patterns) {
    final String modulePath = '/' + simpleModuleName;
    for (int i = 0; i < patterns.size(); i++) {
      String pattern = patterns.get(i);
      if (pattern.startsWith("/")) {
        pattern = modulePath + pattern;
      } else {
        pattern = modulePath + '/' + pattern;
      }
      patterns.set(i, pattern);
    }
  }
}
