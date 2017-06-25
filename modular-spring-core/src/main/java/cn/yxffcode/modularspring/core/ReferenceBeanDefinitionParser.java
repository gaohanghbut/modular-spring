package cn.yxffcode.modularspring.core;

import com.google.common.base.Strings;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
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

    final RootBeanDefinition bean = new RootBeanDefinition();
    bean.setBeanClass(ServiceReference.class);
    try {
      bean.getConstructorArgumentValues().addIndexedArgumentValue(0, Class.forName(anInterface));
    } catch (ClassNotFoundException e) {
      parserContext.getReaderContext().fatal("引用服务失败", e);
    }
    bean.getConstructorArgumentValues().addIndexedArgumentValue(1, anInterface);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(2, uniqueId);

    parserContext.registerBeanComponent(new BeanComponentDefinition(new BeanDefinitionHolder(bean, name)));
    return null;
  }
}
