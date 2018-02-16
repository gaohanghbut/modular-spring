package cn.yxffcode.modularspring.plugin.api;

import org.springframework.beans.factory.xml.BeanDefinitionParser;

/**
 * @author gaohang on 2/10/18.
 */
public abstract class PluginDefBeanDefinitionParser implements BeanDefinitionParser {
  public abstract String getTagName();
}
