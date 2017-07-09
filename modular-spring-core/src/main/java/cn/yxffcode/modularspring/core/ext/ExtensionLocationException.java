package cn.yxffcode.modularspring.core.ext;

/**
 * @author gaohang on 7/9/17.
 */
public class ExtensionLocationException extends RuntimeException {
  public ExtensionLocationException(String message) {
    super(message);
  }

  public ExtensionLocationException(String message, Throwable cause) {
    super(message, cause);
  }

}
