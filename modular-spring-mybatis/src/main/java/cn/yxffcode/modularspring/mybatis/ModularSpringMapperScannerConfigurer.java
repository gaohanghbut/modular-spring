package cn.yxffcode.modularspring.mybatis;

import cn.yxffcode.modularspring.core.config.ModularBeanUtils;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @deprecated 可使用@ModularService代替
 * @author gaohang on 9/9/17.
 */
@Deprecated
public class ModularSpringMapperScannerConfigurer extends MapperScannerConfigurer {
  private final BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

  private boolean createModularService;

  @Override
  public void postProcessBeanDefinitionRegistry(final BeanDefinitionRegistry registry) {
    if (!createModularService) {
      super.postProcessBeanDefinitionRegistry(registry);
    }
    super.postProcessBeanDefinitionRegistry(Reflection.newProxy(BeanDefinitionRegistry.class, new AbstractInvocationHandler() {
      @Override
      protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        if (Objects.equals("registerBeanDefinition", method.getName())) {
          //registry modular spring service
          final String beanName = (String) args[0];
          final BeanDefinition bean = (BeanDefinition) args[1];
          final RootBeanDefinition serviceBean = ModularBeanUtils.buildServiceBean(beanName,
              bean.getBeanClassName(), null);
          registry.registerBeanDefinition(
              beanNameGenerator.generateBeanName(serviceBean, registry), serviceBean);
        }

        return method.invoke(registry, args);
      }
    }));
  }

  public boolean isCreateModularService() {
    return createModularService;
  }

  public void setCreateModularService(boolean createModularService) {
    this.createModularService = createModularService;
  }
}
