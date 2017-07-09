package cn.yxffcode.modularspring.core.ext;

import cn.yxffcode.modularspring.core.ServiceBean;
import com.google.common.base.Strings;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author gaohang on 7/9/17.
 */
public class ExtensionBeanDefinitionParser implements BeanDefinitionParser {
  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    final String extensionName = element.getAttribute("name");
    final String interfaceName = element.getAttribute("interface");

    final RootBeanDefinition bean = new RootBeanDefinition();
    bean.setBeanClass(ExtensionFactoryBean.class);
    bean.getConstructorArgumentValues().addIndexedArgumentValue(0, extensionName);
    try {
      bean.getConstructorArgumentValues().addIndexedArgumentValue(1, Class.forName(interfaceName));
    } catch (ClassNotFoundException e) {
      parserContext.getReaderContext().fatal("找不到类" + interfaceName, e);
    }
    return null;
  }
}
