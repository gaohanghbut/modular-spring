package cn.yxffcode.modularspring.http.cfg;

import cn.yxffcode.modularspring.http.*;
import cn.yxffcode.modularspring.http.http.DefaultHttpClientFactory;
import cn.yxffcode.modularspring.http.http.DefaultHttpExecutor;
import cn.yxffcode.modularspring.http.http.HttpClientFactory;
import cn.yxffcode.modularspring.http.http.HttpExecutor;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 8/6/17.
 */
public class Configuration {

  private final List<RequestPostProcessor> commonRequestPostProcessors;
  private final Map<String, MappedRequest> mappedRequests;
  private final Multimap<String, RequestPostProcessor> requestPostProcessors;
  private final Map<String, ResponseHandler> responseHandlers;
  private final HttpExecutor httpExecutor;
  private final ResponseHandler defaultResponseHandler;
  private ExecutorService callbackExecutor;
  private Configuration(Map<String, MappedRequest> mappedRequests,
                        Multimap<String, RequestPostProcessor> requestPostProcessors,
                        Map<String, ResponseHandler> responseHandlers,
                        ResponseHandler defaultResponseHandler,
                        HttpClientFactory httpClientFactory,
                        List<RequestPostProcessor> commonRequestPostProcessors,
                        ExecutorService callbackExecutor) {
    this.mappedRequests = mappedRequests;
    this.requestPostProcessors = requestPostProcessors;
    this.responseHandlers = responseHandlers;
    this.defaultResponseHandler = defaultResponseHandler;
    this.httpExecutor = new DefaultHttpExecutor(httpClientFactory, this);
    this.commonRequestPostProcessors = Collections.unmodifiableList(commonRequestPostProcessors);
    this.callbackExecutor = callbackExecutor;
  }

  public static ConfigurationBuilder newBuilder() {
    return new ConfigurationBuilder();
  }

  public HttpExecutor getHttpExecutor() {
    return httpExecutor;
  }

  public List<RequestPostProcessor> getCommonRequestPostProcessors() {
    return commonRequestPostProcessors;
  }

  public ResponseHandler getResponseHandler(String mrId) {
    final ResponseHandler responseHandler = responseHandlers.get(mrId);
    return responseHandler != null ? responseHandler : defaultResponseHandler;
  }

  public ExecutorService getCallbackExecutor() {
    if (callbackExecutor == null) {
      synchronized (this) {
        if (callbackExecutor == null) {
          callbackExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            private final ThreadFactory threadFactory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
              final Thread thread = threadFactory.newThread(r);
              thread.setDaemon(true);
              return thread;
            }
          });
        }
      }
    }
    return callbackExecutor;
  }

  public ResponseHandler getDefaultResponseHandler() {
    return defaultResponseHandler;
  }

  public <T> T newMapper(Class<T> mapperClass) {
    return MappedProxy.newProxy(this, mapperClass);
  }

  public Iterable<RequestPostProcessor> getPostProcessors(String mrId) {
    return requestPostProcessors.get(mrId);
  }

  public MappedRequest getMappedRequest(String mrId) {
    return mappedRequests.get(mrId);
  }

  public static final class ConfigurationBuilder {
    private final Map<String, MappedRequest> mappedRequests = Maps.newHashMap();
    private final Map<Class<?>, RequestPostProcessor> postProcessorInstances = Maps.newHashMap();
    private final Map<Class<?>, ResponseHandler> typeToresponseHandlers = Maps.newHashMap();
    private final Map<String, ResponseHandler> responseHandlers = Maps.newHashMap();
    private final Multimap<String, RequestPostProcessor> requestPostProcessors = HashMultimap.create();
    private ResponseHandler defaultResponseHandler;
    private HttpClientFactory httpClientFactory;
    private List<RequestPostProcessor> commonRequestPostProcessors = Lists.newArrayList();
    private ExecutorService callbackExecutor;

    private ConfigurationBuilder() {
    }

    public ConfigurationBuilder setHttpClientFactory(HttpClientFactory httpClientFactory) {
      this.httpClientFactory = httpClientFactory;
      return this;
    }

    public ConfigurationBuilder setCallbackExecutor(ExecutorService callbackExecutor) {
      this.callbackExecutor = callbackExecutor;
      return this;
    }

    public ConfigurationBuilder addCommonRequestPostProcessor(RequestPostProcessor requestPostProcessor) {
      checkNotNull(requestPostProcessor);
      commonRequestPostProcessors.add(requestPostProcessor);
      return this;
    }

    public ConfigurationBuilder addRequestPostProcessor(String mrId, Class<? extends RequestPostProcessor>[] types) {
      checkNotNull(mrId);
      checkNotNull(types);

      try {
        for (Class<? extends RequestPostProcessor> type : types) {
          final RequestPostProcessor existsProcessor = postProcessorInstances.get(type);
          if (existsProcessor != null) {
            requestPostProcessors.put(mrId, existsProcessor);
            continue;
          }
          final RequestPostProcessor requestPostProcessor = type.newInstance();
          postProcessorInstances.put(type, requestPostProcessor);
          requestPostProcessors.put(mrId, requestPostProcessor);
        }
        return this;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public ConfigurationBuilder setDefaultResponseHandler(ResponseHandler responseHandler) {
      this.defaultResponseHandler = responseHandler;
      return this;
    }

    public ConfigurationBuilder addMappedRequest(MappedRequest request) {
      checkNotNull(request);
      mappedRequests.put(request.getId(), request);
      return this;
    }

    public ConfigurationBuilder addResponseHandler(String mrId, Class<? extends ResponseHandler> responseHandlerType) {
      checkNotNull(responseHandlerType);
      if (!typeToresponseHandlers.containsKey(responseHandlerType)) {
        try {
          typeToresponseHandlers.put(responseHandlerType, responseHandlerType.newInstance());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      responseHandlers.put(mrId, typeToresponseHandlers.get(responseHandlerType));
      return this;
    }

    public ConfigurationBuilder parse(Class<?> mapperClass) {
      checkNotNull(mapperClass);

      final Method[] methods = mapperClass.getMethods();
      if (methods == null || methods.length == 0) {
        return this;
      }
      final PostProcessors typeProcessors = mapperClass.getDeclaredAnnotation(PostProcessors.class);
      for (Method method : methods) {
        final Request request = method.getDeclaredAnnotation(Request.class);
        if (request == null) {
          //不需要mapper
          continue;
        }
        final MappedRequest.MappedRequestBuilder mappedRequestBuilder =
            MappedRequest.newBuilder(method);
        final RequestInfo requestInfo = new RequestInfo(request);
        mappedRequestBuilder.setRequestInfo(requestInfo);

        final String mrId = MappedRequestUtils.buildMappedRequestId(mapperClass, method);
        mappedRequestBuilder.setId(mrId);

        //detect request method
        final GET get = method.getDeclaredAnnotation(GET.class);
        if (get != null) {
          mappedRequestBuilder.setHttpMethod(HttpMethod.GET);
        }
        final POST post = method.getDeclaredAnnotation(POST.class);
        if (post != null) {
          mappedRequestBuilder.setHttpMethod(HttpMethod.POST);
          mappedRequestBuilder.setEntityType(post.entity());
        }
        final PUT put = method.getDeclaredAnnotation(PUT.class);
        if (put != null) {
          mappedRequestBuilder.setHttpMethod(HttpMethod.PUT);
        }
        final DELETE delete = method.getDeclaredAnnotation(DELETE.class);
        if (delete != null) {
          mappedRequestBuilder.setHttpMethod(HttpMethod.DELETE);
        }

        Response response = method.getDeclaredAnnotation(Response.class);
        if (response == null) {
          response = mapperClass.getDeclaredAnnotation(Response.class);
        }
        if (response != null) {
          addResponseHandler(mrId, response.value());
        }

        addMappedRequest(mappedRequestBuilder.build());

        if (typeProcessors != null) {
          addRequestPostProcessor(mrId, typeProcessors.value());
        }
        final PostProcessors postProcessors = method.getDeclaredAnnotation(PostProcessors.class);
        if (postProcessors != null) {
          addRequestPostProcessor(mrId, postProcessors.value());
        }
      }
      return this;
    }

    public Configuration build() {
      if (defaultResponseHandler == null) {
        defaultResponseHandler = new FastJsonResponseHandler();
      }
      if (httpClientFactory == null) {
        this.httpClientFactory = new DefaultHttpClientFactory();
      }
      return new Configuration(mappedRequests, requestPostProcessors,
          responseHandlers, defaultResponseHandler, httpClientFactory,
          commonRequestPostProcessors, callbackExecutor);
    }
  }

}
