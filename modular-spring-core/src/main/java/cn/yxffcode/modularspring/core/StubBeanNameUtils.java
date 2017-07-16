package cn.yxffcode.modularspring.core;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 7/16/17.
 */
public final class StubBeanNameUtils {
  private StubBeanNameUtils() {
  }

  public static String getStubNameForInterface(Class<?> interfaceType) {
    checkNotNull(interfaceType);
    if (!interfaceType.isInterface()) {
      throw new IllegalArgumentException(interfaceType.getCanonicalName() + " 不是接口");
    }
    return getStubNameForInterfaceName(interfaceType.getCanonicalName());
  }

  public static String getStubNameForInterfaceName(String interfaceName) {
    return interfaceName + "#stub";
  }
}
