package cn.yxffcode.modularspring.core.config;

import cn.yxffcode.modularspring.core.ext.ExtensionBeanDefinitionParser;
import cn.yxffcode.modularspring.core.ext.ExtensionHandlerBeanDefinitionParser;
import cn.yxffcode.modularspring.core.ext.ExtensionPointBeanDefinitionParser;
import cn.yxffcode.modularspring.plugin.api.PluginDefBeanDefinitionParser;
import cn.yxffcode.modularspring.plugin.api.PluginTools;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
    registerBeanDefinitionParser("auto-stub", new AutoStubBeanDefinitionParser());
    registerBeanDefinitionParser("stub", new StubBeanDefinitionParser());
    registerBeanDefinitionParser("plugin", new PluginBeanDefinitionParser());
    registerBeanDefinitionParser("extension-handler", new ExtensionHandlerBeanDefinitionParser());
  }
}
