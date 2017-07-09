package cn.yxffcode.modularspring.core;

import cn.yxffcode.modularspring.core.ext.ExtensionBeanDefinitionParser;
import cn.yxffcode.modularspring.core.ext.ExtensionPointBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author gaohang on 6/24/17.
 */
public class ModularNamespaceHandler extends NamespaceHandlerSupport {
  @Override
  public void init() {
    registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());
    registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());
    registerBeanDefinitionParser("extension", new ExtensionBeanDefinitionParser());
    registerBeanDefinitionParser("extension-point", new ExtensionPointBeanDefinitionParser());
  }
}
