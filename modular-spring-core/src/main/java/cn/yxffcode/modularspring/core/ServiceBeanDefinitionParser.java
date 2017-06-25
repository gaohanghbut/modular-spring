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
final class ServiceBeanDefinitionParser implements BeanDefinitionParser {

  private final BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    final String ref = element.getAttribute("ref");
    final String anInterface = element.getAttribute("interface");
    final String uniqueId = element.getAttribute("unique-id");

    final RootBeanDefinition bean = new RootBeanDefinition();
    bean.setBeanClass(ServiceBean.class);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(0, new RuntimeBeanNameReference(ref));
    bean.getConstructorArgumentValues().addIndexedArgumentValue(1, anInterface);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(2, Strings.nullToEmpty(uniqueId));

    parserContext.registerBeanComponent(new BeanComponentDefinition(new BeanDefinitionHolder(bean, beanNameGenerator.generateBeanName(bean, parserContext.getRegistry()))));
    return null;
  }
}
