package cn.yxffcode.modularspring.core.config;

import cn.yxffcode.modularspring.plugin.api.PluginDefBeanDefinitionParser;
import cn.yxffcode.modularspring.plugin.api.PluginTools;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Map;

/**
 * @author gaohang on 2/16/18.
 */
public class PluginBeanDefinitionParser implements BeanDefinitionParser {
  private final Map<String, BeanDefinitionParser> beanDefinitionParsers = Maps.newHashMap();

  public PluginBeanDefinitionParser() {

    final List<PluginDefBeanDefinitionParser> beanDefinitionParsers = PluginTools.getBeanDefinitionParsers();
    if (CollectionUtils.isEmpty(beanDefinitionParsers)) {
      return;
    }
    for (PluginDefBeanDefinitionParser beanDefinitionParser : beanDefinitionParsers) {
      registerBeanDefinitionParser(beanDefinitionParser.getTagName(), beanDefinitionParser);
    }
  }

  private void registerBeanDefinitionParser(String tagName, PluginDefBeanDefinitionParser beanDefinitionParser) {
    beanDefinitionParsers.put(tagName, beanDefinitionParser);
  }

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    final NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      final Node node = childNodes.item(i);
      if (!(node instanceof Element)) {
        continue;
      }
      final BeanDefinitionParser beanDefinitionParser = beanDefinitionParsers.get(node.getNodeName());
      if (beanDefinitionParser == null) {
        throw new RuntimeException("BeanDefinitionParser not found for tag:" + node.getNodeName());
      }
      beanDefinitionParser.parse((Element) node, parserContext);
    }
    return null;
  }
}
