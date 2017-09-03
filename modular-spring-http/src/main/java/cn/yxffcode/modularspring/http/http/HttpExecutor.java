package cn.yxffcode.modularspring.http.http;

import cn.yxffcode.modularspring.http.MappedRequest;
import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * @author gaohang on 8/5/17.
 */
public interface HttpExecutor {
  Future<HttpResponse> execute(final MappedRequest mappedRequest, Object params);
}
