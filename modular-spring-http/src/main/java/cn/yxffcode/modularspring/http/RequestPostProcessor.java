package cn.yxffcode.modularspring.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.Map;

/**
 * @author gaohang on 8/6/17.
 */
public interface RequestPostProcessor {
  boolean postProcessRequest(HttpUriRequest request, MappedRequest mr, Map<String, Object> params);

  void postProcessResponse(HttpResponse response, MappedRequest mr);
}
