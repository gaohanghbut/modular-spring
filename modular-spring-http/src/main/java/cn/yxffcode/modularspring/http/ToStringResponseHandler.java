package cn.yxffcode.modularspring.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * 处理http返回的内容
 *
 * @author gaohang on 8/11/17.
 */
public class ToStringResponseHandler implements ResponseHandler {
  @Override
  public Object handle(MappedRequest request, HttpResponse response) {
    final HttpEntity entity = response.getEntity();
    try {
      return EntityUtils.toString(entity);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
