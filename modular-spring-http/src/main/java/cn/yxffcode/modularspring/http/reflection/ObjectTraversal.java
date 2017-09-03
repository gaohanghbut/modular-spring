package cn.yxffcode.modularspring.http.reflection;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import java.lang.reflect.Field;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 8/8/17.
 */
public final class ObjectTraversal {
  private static final Splitter DOT = Splitter.on('.').trimResults();

  public static ObjectTraversal wrap(final Object obj) {
    checkNotNull(obj);
    return new ObjectTraversal(obj);
  }

  private final Object target;

  private ObjectTraversal(Object target) {
    this.target = target;
  }

  public Object get(String path) {
    checkArgument(!Strings.isNullOrEmpty(path));
    final Iterable<String> subPaths = DOT.split(path);
    try {
      Object v = target;
      for (String subPath : subPaths) {
        if (v instanceof Map) {
          v = ((Map) v).get(subPath);
          continue;
        }
        final Field field = v.getClass().getDeclaredField(subPath);
        field.setAccessible(true);
         v = field.get(target);
      }
      return v;
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
