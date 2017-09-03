package cn.yxffcode.modularspring.http.cfg;

import java.lang.reflect.Method;

/**
 * @author gaohang on 8/7/17.
 */
public abstract class MappedRequestUtils {
  private MappedRequestUtils() {
  }

  public static String buildMappedRequestId(Class<?> type, Method method) {
    return type.getCanonicalName() + '.' + method.getName();
  }
}
