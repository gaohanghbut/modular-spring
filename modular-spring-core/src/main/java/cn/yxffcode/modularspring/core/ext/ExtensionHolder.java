package cn.yxffcode.modularspring.core.ext;

import com.google.common.collect.Maps;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 7/9/17.
 */
final class ExtensionHolder {
  private ExtensionHolder() {
  }

  private static final Map<String, ExtensionPointBean> extensionPointBeanMap = Maps.newHashMap();

  public static void registryExtentionPoint(ExtensionPointBean extensionPointBean) {
    checkNotNull(extensionPointBean);
    extensionPointBeanMap.put(extensionPointBean.getBeanName(), extensionPointBean);
  }
  public static ExtensionPointBean getExtensionPoint(String extensionName) {
    return extensionPointBeanMap.get(extensionName);
  }
}
