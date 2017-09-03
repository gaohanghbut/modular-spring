package cn.yxffcode.modularspring.http;

import cn.yxffcode.modularspring.http.cfg.Configuration;
import cn.yxffcode.modularspring.http.cfg.MappedRequestUtils;
import cn.yxffcode.modularspring.http.http.HttpExecutor;
import com.google.common.collect.Maps;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.http.HttpResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author gaohang on 8/7/17.
 */
public class MappedProxy extends AbstractInvocationHandler {

  public static <T> T newProxy(Configuration configuration, Class<T> interfaceType) {
    return Reflection.newProxy(interfaceType, new MappedProxy(configuration, interfaceType));
  }

  private final Configuration configuration;
  private final Class<?> mapperClass;
  private final HttpExecutor httpExecutor;

  private MappedProxy(Configuration configuration, Class<?> mapperClass) {
    this.configuration = configuration;
    this.mapperClass = mapperClass;
    this.httpExecutor = configuration.getHttpExecutor();
  }

  @Override
  protected Object handleInvocation(Object o, Method method, Object[] args) throws Throwable {
    final String mrId = MappedRequestUtils.buildMappedRequestId(mapperClass, method);
    final MappedRequest mappedRequest = configuration.getMappedRequest(mrId);
    if (mappedRequest == null) {
      throw new NoSuchHttpRequestException("http mapper不存在：" + method);
    }
    final Object paramObject = resolveRequestParameter(method, args);
    final Future<HttpResponse> future = httpExecutor.execute(mappedRequest, paramObject);

    //callback
    if (void.class == method.getReturnType()) {
      //检查是否有callback
      return prepareCallback(args, mrId, mappedRequest, future);
    }

    //无泛型
    final Type returnType = method.getGenericReturnType();
    if (returnType instanceof Class) {
      if (Future.class == returnType) {
        return future;
      }
      if (HttpResponse.class.isAssignableFrom((Class<?>) returnType)) {
        return mappedRequest.getRequestInfo().getTimeout() <= 0 ? future.get()
            : future.get(mappedRequest.getRequestInfo().getTimeout(), TimeUnit.MILLISECONDS);
      }
    }

    //泛型，但类型参数是HttpResponse
    if (returnType instanceof ParameterizedType) {
      final Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
      if (actualTypeArguments.length == 1 && actualTypeArguments[0] == HttpResponse.class) {
        return future;
      }
    }
    //有泛型，返回Future
    final Future resultFuture = Reflection.newProxy(Future.class, new ResponseWrapper(future, mrId, mappedRequest));
    if (Future.class == method.getReturnType()) {
      return resultFuture;
    }
    //返回的不是future
    return mappedRequest.getRequestInfo().getTimeout() <= 0 ? resultFuture.get()
        : resultFuture.get(mappedRequest.getRequestInfo().getTimeout(), TimeUnit.MILLISECONDS);
  }

  private Object prepareCallback(Object[] args, String mrId, MappedRequest mappedRequest, Future<HttpResponse> future) {
    if (args != null && args.length != 0 && args[args.length - 1] != null
        && args[args.length - 1] instanceof ResponseCallback) {
      final ResponseCallback callback = (ResponseCallback) args[args.length - 1];
      final Future resultFuture = Reflection.newProxy(Future.class, new ResponseWrapper(future, mrId, mappedRequest));
      final ListenableFuture listenableFuture = JdkFutureAdapters.listenInPoolThread(resultFuture);
      listenableFuture.addListener(new Runnable() {
        @Override
        public void run() {
          try {
            callback.apply(listenableFuture.get());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }, configuration.getCallbackExecutor());
    }
    return null;
  }

  private Object resolveRequestParameter(Method method, Object[] args) {
    Object paramObject;
    if (args == null || args.length == 0) {
      paramObject = Collections.emptyMap();
    } else {
      final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      if (parameterAnnotations == null) {
        if (args.length == 1) {
          paramObject = args[0];
        } else {
          paramObject = Collections.emptyMap();
        }
      } else {
        final Map<String, Object> tmpParams = Maps.newHashMapWithExpectedSize(args.length);
        outer:
        for (int i = 0; i < parameterAnnotations.length; i++) {
          Annotation[] annotations = parameterAnnotations[i];
          for (Annotation annotation : annotations) {
            if (annotation instanceof HttpParam) {
              tmpParams.put(((HttpParam) annotation).value(), args[i]);
              continue outer;
            }
          }
        }
        paramObject = tmpParams;
      }
    }
    return paramObject;
  }

  private class ResponseWrapper extends AbstractInvocationHandler {
    private final Future<HttpResponse> future;
    private final String mrId;
    private final MappedRequest mappedRequest;
    private Object result;

    public ResponseWrapper(Future<HttpResponse> future, String mrId, MappedRequest mappedRequest) {
      this.future = future;
      this.mrId = mrId;
      this.mappedRequest = mappedRequest;
    }

    @Override
    protected Object handleInvocation(Object target, Method method, Object[] args) throws Throwable {
      if (!method.getName().equals("get")) {
        return method.invoke(future, args);
      }
      if (result != null) {
        return result;
      }
      final HttpResponse httpResponse = (HttpResponse) method.invoke(future, args);
      if (httpResponse.getStatusLine().getStatusCode() != 200) {
        throw new RequestFaildException("请求出错，status=" + httpResponse.getStatusLine().getStatusCode());
      }
      final ResponseHandler responseHandler = configuration.getResponseHandler(mrId);

      return result = responseHandler.handle(mappedRequest, httpResponse);
    }
  }
}
