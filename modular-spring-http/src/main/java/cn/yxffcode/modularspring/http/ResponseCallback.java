package cn.yxffcode.modularspring.http;

/**
 * @author gaohang on 8/13/17.
 */
public interface ResponseCallback<T> {
  void apply(T result);
}
