package cn.yxffcode.modularspring.http;

/**
 * @author gaohang on 8/6/17.
 */
public class StopRequestException extends RuntimeException {
  public StopRequestException() {
  }

  public StopRequestException(String message) {
    super(message);
  }

  public StopRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public StopRequestException(Throwable cause) {
    super(cause);
  }

  public StopRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
