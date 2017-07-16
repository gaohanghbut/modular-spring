package cn.yxffcode.modularspring.core.config;

import cn.yxffcode.modularspring.core.AutoStubCreator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author gaohang on 7/16/17.
 */
public class AutoStubBeanDefinitionParser implements BeanDefinitionParser {
  private static final String STUB_PROXY_FACTORY_BEAN_NAME = "modularAutoStubFactoryBean";

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    //注册自动代理
    final String active = element.getAttribute("active");
    boolean isAutoStubActive = StringUtils.isBlank(active) ? true : Boolean.parseBoolean(active);
    if (!isAutoStubActive) {
      return null;
    }
    if (parserContext.getRegistry().containsBeanDefinition(STUB_PROXY_FACTORY_BEAN_NAME)) {
      return null;
    }
    //配置代理工厂
    final RootBeanDefinition bean = new RootBeanDefinition(AutoStubCreator.class);
    parserContext.registerBeanComponent(new BeanComponentDefinition(new BeanDefinitionHolder(bean, STUB_PROXY_FACTORY_BEAN_NAME)));
    return null;
  }
}
