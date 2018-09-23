package cn.yxffcode.modularspring.core.ext.annotation;

import cn.yxffcode.modularspring.core.ext.ExtensionHandlerBean;
import cn.yxffcode.modularspring.core.ext.ExtensionLocationException;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author gaohang
 */
public class ExtAnnotationRegistryProcessor implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        for (String beanName : beanDefinitionRegistry.getBeanDefinitionNames()) {
            AbstractBeanDefinition beanDefinition = (AbstractBeanDefinition) beanDefinitionRegistry.getBeanDefinition(beanName);

            Class<?> beanType = getBeanType(beanDefinition);
            if (beanType == null) {
                continue;
            }
            ExtensionHandler extensionHandler = beanType.getAnnotation(ExtensionHandler.class);
            if (extensionHandler == null) {
                continue;
            }
            //listener methods

            final List<ExtensionHandlerBean.ListenerMethod> listenerMethods = Lists.newArrayList();
            final Method[] methods = beanType.getMethods();
            for (Method method : methods) {
                ExtensionListener extensionListener = method.getAnnotation(ExtensionListener.class);
                Class<?> type = extensionListener.value();
                if (type == Object.class) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != 1) {
                        throw new ExtensionLocationException("extension handler listener methods must have only one parameter");
                    }
                    type = parameterTypes[0];
                }
                listenerMethods.add(new ExtensionHandlerBean.ListenerMethod(method.getName(), type));
            }
            //注册extension-handler
            final RootBeanDefinition bean = new RootBeanDefinition();
            bean.setBeanClass(ExtensionHandlerBean.class);
            bean.getConstructorArgumentValues().addIndexedArgumentValue(0, StringUtils.isBlank(extensionHandler.value()) ? beanName : extensionHandler.value());
            bean.getConstructorArgumentValues().addIndexedArgumentValue(1, beanName);
            bean.getConstructorArgumentValues().addIndexedArgumentValue(2, listenerMethods);
        }
    }

    private Class<?> getBeanType(AbstractBeanDefinition beanDefinition) {
        Class<?> beanType;
        String beanClassName = beanDefinition.getBeanClassName();
        try {
            if (StringUtils.isNotBlank(beanClassName)) {
                beanType = Class.forName(beanClassName);
            } else {
                beanType = beanDefinition.getBeanClass();
            }
        } catch (ClassNotFoundException e) {
            beanType = null;
        }
        return beanType;
    }

}
