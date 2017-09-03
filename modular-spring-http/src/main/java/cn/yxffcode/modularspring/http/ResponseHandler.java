package cn.yxffcode.modularspring.http;

import org.apache.http.HttpResponse;

/**
 * @author gaohang on 8/11/17.
 */
public interface ResponseHandler {
  Object handle(MappedRequest request, HttpResponse response);
}
