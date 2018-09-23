package cn.yxffcode.modularspring.core.ext;

import cn.yxffcode.modularspring.core.ext.utils.ExtensionPointUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
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

  private static final BeanNameGenerator HANDLER_CONF_BEAN_NAME_GENERATOR = new DefaultBeanNameGenerator();

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    final String extensionName = element.getAttribute("name");
    final String ref = element.getAttribute("handler-bean-ref");

    NodeList listenerMethods = element.getChildNodes();
    List<ExtensionHandlerBean.ListenerMethod> listenerMethodBeans = Lists.newArrayList();
    try {
      for (int i = 0, j = listenerMethods.getLength(); i < j; i++) {
        final Node item = listenerMethods.item(i);
        if (!(item instanceof Element)) {
          continue;
        }
        Element listenerMethod = (Element) item;
        if (!"listener-method".equals(listenerMethod.getLocalName())) {
          continue;
        }
        listenerMethodBeans.add(new ExtensionHandlerBean.ListenerMethod(
            listenerMethod.getAttribute("name"), Class.forName(listenerMethod.getAttribute("extension-type"))));
      }
    } catch (ClassNotFoundException e) {
      parserContext.getReaderContext().fatal("找不到类", e);
    }

    ExtensionPointUtils.registryExtensionHandler(parserContext.getRegistry(), extensionName, ref, listenerMethodBeans);
    return null;
  }
}
