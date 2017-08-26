package cn.yxffcode.modularspring.core.ext;

import com.google.common.collect.Lists;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 用于创建扩展点的代理
 *
 * @author gaohang on 7/9/17.
 */
public class ExtensionFactoryBean implements FactoryBean<Object> {

  private final String extensionName;
  private final Class<?> extensionInterface;

  public ExtensionFactoryBean(String extensionName, Class<?> extensionInterface) {
    this.extensionName = extensionName;
    this.extensionInterface = extensionInterface;
  }

  @Override
  public Object getObject() throws Exception {
    return Reflection.newProxy(extensionInterface, new AbstractInvocationHandler() {
      private Object target;

      @Override
      protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        if (target == null) {
          synchronized (this) {
            if (target == null) {
              //获取真实的扩展点
              final Collection<ExtensionPointBean> extensionPoints = ExtensionHolder.getExtensionPoint(extensionName);
              if (CollectionUtils.isEmpty(extensionPoints)) {
                throw new ExtensionLocationException(
                    "没有找到扩展点的实现,请检查是否配置了扩展点的实现.或者检查扩展点的实现模块不应该依赖扩展点所在的模块.扩展点名:" + extensionName);
              }
              if (extensionPoints.size() == 1) {
                final ExtensionPointBean extensionPoint = extensionPoints.iterator().next();
                target = extensionPoint.getApplicationContext().getBean(extensionPoint.getBeanName(), extensionInterface);
              } else if (ExtensionContainer.class.isAssignableFrom(extensionInterface)) {
                final List<Object> extensionPointInstances = Lists.newArrayListWithCapacity(extensionPoints.size());

                for (ExtensionPointBean extensionPoint : extensionPoints) {
                  extensionPointInstances.add(extensionPoint.getApplicationContext().getBean(extensionPoint.getBeanName()));
                }


                target = (ExtensionContainer<Object>) () -> Collections.unmodifiableList(extensionPointInstances);
              }
            }
          }
        }
        return method.invoke(target, args);
      }
    });
  }

  @Override
  public Class<?> getObjectType() {
    return extensionInterface;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
