package cn.yxffcode.modularspring.core.ext;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

/**
 * @author gaohang on 7/9/17.
 */
public class ExtensionHandlerBeanDefinitionParser implements BeanDefinitionParser {
  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    final String extensionName = element.getAttribute("name");
    final String ref = element.getAttribute("handler-bean-ref");

    NodeList listenerMethods = element.getElementsByTagName("listener-method");
    List<ExtensionHandlerBean.ListenerMethod> listenerMethodBeans = Lists.newArrayList();
    try {
      for (int i = 0, j = listenerMethods.getLength(); i < j; i++) {
        Element listenerMethod = (Element) listenerMethods.item(i);
        listenerMethodBeans.add(new ExtensionHandlerBean.ListenerMethod(
            listenerMethod.getAttribute("name"), Class.forName(listenerMethod.getAttribute("extension-type"))));
      }
    } catch (ClassNotFoundException e) {
      parserContext.getReaderContext().fatal("找不到类", e);
    }

    final RootBeanDefinition bean = new RootBeanDefinition();
    bean.setBeanClass(ExtensionHandlerBean.class);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(0, extensionName);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(1, ref);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(2, listenerMethodBeans);

    parserContext.registerBeanComponent(new BeanComponentDefinition(new BeanDefinitionHolder(bean, extensionName)));
    return null;
  }
}
