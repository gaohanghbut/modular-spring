package cn.yxffcode.modularspring.core.ext;

import java.util.List;

/**
 * @author gaohang on 8/26/17.
 */
public interface ExtensionContainer<T> {
  List<T> getExtensions();
}
