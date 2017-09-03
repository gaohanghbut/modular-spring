package cn.yxffcode.modularspring.http.text;

/**
 * @author gaohang on 8/16/16.
 */
public class PlaceNotFoundException extends RuntimeException {
  public PlaceNotFoundException(String message) {
    super(message);
  }
}
