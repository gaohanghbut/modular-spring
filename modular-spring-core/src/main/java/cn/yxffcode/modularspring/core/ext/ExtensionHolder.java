package cn.yxffcode.modularspring.core.ext;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 7/9/17.
 */
final class ExtensionHolder {
  private ExtensionHolder() {
  }

  private static final Multimap<String, ExtensionPointBean> extensionPointBeanMap = HashMultimap.create();

  public static void registryExtensionPoint(ExtensionPointBean extensionPointBean) {
    checkNotNull(extensionPointBean);
    extensionPointBeanMap.put(extensionPointBean.getBeanName(), extensionPointBean);
  }

  public static Collection<ExtensionPointBean> getExtensionPoint(String extensionName) {
    return extensionPointBeanMap.get(extensionName);
  }
}
