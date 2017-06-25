package cn.yxffcode.modularspring.core;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author gaohang on 6/24/17.
 */
public class ModularNamespaceHandler extends NamespaceHandlerSupport {
  @Override
  public void init() {
    registerBeanDefinitionParser("service", new ServiceBeanDefinitionParser());
    registerBeanDefinitionParser("reference", new ReferenceBeanDefinitionParser());
  }
}
