package cn.yxffcode.modularspring.http.http;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

/**
 * @author gaohang on 8/6/17.
 */
public class DefaultHttpClientFactory implements HttpClientFactory {
  @Override
  public CloseableHttpAsyncClient create() {
    return HttpAsyncClients.createDefault();
  }
}
