package cn.yxffcode.modularspring.plugin.api;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author gaohang on 2/15/18.
 */
public abstract class PluginTools {
  private PluginTools() {
  }

  private static final List<PluginDefBeanDefinitionParser> beanDefinitionParsers = Lists.newArrayList();

  public static void registryBeanDefinitionParser(PluginDefBeanDefinitionParser beanDefinitionParser) {
    beanDefinitionParsers.add(beanDefinitionParser);
  }

  public static List<PluginDefBeanDefinitionParser> getBeanDefinitionParsers() {
    return beanDefinitionParsers;
  }
}
