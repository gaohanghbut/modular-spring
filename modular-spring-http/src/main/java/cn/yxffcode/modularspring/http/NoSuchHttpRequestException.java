package cn.yxffcode.modularspring.http;

/**
 * @author gaohang on 8/7/17.
 */
public class NoSuchHttpRequestException extends RuntimeException {
  public NoSuchHttpRequestException() {
  }

  public NoSuchHttpRequestException(String message) {
    super(message);
  }

  public NoSuchHttpRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoSuchHttpRequestException(Throwable cause) {
    super(cause);
  }

  public NoSuchHttpRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
