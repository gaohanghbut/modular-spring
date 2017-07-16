package cn.yxffcode.modularspring.core.config;

import cn.yxffcode.modularspring.core.StubBeanNameUtils;
import cn.yxffcode.modularspring.core.StubFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author gaohang on 7/16/17.
 */
public class StubBeanDefinitionParser implements BeanDefinitionParser {
  private static final String STUB_PROXY_FACTORY_BEAN_NAME = "modularAutoStubFactoryBean";

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    final String interfaceName = element.getAttribute("interface");
    final RootBeanDefinition bean = new RootBeanDefinition(StubFactoryBean.class);
    try {
      bean.getConstructorArgumentValues().addIndexedArgumentValue(0, Class.forName(interfaceName));
    } catch (ClassNotFoundException e) {
      parserContext.getReaderContext().fatal("无法找到类", e);
    }
    final String ref = element.getAttribute("invocation-handler-ref");
    if (StringUtils.isNotBlank(ref)) {
      bean.getPropertyValues().addPropertyValue("invocationHandlerRef", new RuntimeBeanNameReference(ref));
    }

    final String handlerClassName = element.getAttribute("invocation-handler");
    if (StringUtils.isNotBlank(handlerClassName)) {
      bean.getPropertyValues().addPropertyValue("invocationHandler", handlerClassName);
    }
    parserContext.registerBeanComponent(new BeanComponentDefinition(
        new BeanDefinitionHolder(bean, StubBeanNameUtils.getStubNameForInterfaceName(interfaceName))));

    return null;
  }
}
