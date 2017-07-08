package cn.yxffcode.modularspring.core;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author gaohang on 6/24/17.
 */
final class ReferenceBeanDefinitionParser implements BeanDefinitionParser {

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    final String name = element.getAttribute("name");
    final String anInterface = element.getAttribute("interface");
    final String uniqueId = element.getAttribute("unique-id");

    final RootBeanDefinition bean;
    try {
      bean = ModularBeanUtils.buildReferenceBean(Class.forName(anInterface), uniqueId);
    } catch (ClassNotFoundException e) {
      parserContext.getReaderContext().fatal("引用服务失败", e);
      return null;
    }

    parserContext.registerBeanComponent(new BeanComponentDefinition(new BeanDefinitionHolder(bean, name)));
    return null;
  }

}
