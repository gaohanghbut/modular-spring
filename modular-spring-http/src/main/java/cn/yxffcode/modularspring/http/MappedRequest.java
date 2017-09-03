package cn.yxffcode.modularspring.http;

import cn.yxffcode.modularspring.http.reflection.ObjectTraversal;
import cn.yxffcode.modularspring.http.text.TextTemplate;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author gaohang on 8/6/17.
 */
public class MappedRequest {

  public static MappedRequestBuilder newBuilder(Method method) {
    return new MappedRequestBuilder(method);
  }

  private static final Splitter PARAM_SPLITTER = Splitter.on('=').trimResults();
  private static final Splitter SEPERATE_SPLITTER = Splitter.on('&').trimResults();

  private final String id;
  private final RequestInfo requestInfo;
  private final HttpMethod httpMethod;
  private final EntityType entityType;
  private final TextTemplate textTemplate;
  private final Type returnType;
  private final Map<String, String> queryStringParams;
  private final TextTemplate pureUrlTemplate;

  private MappedRequest(String id,
                        RequestInfo requestInfo,
                        HttpMethod httpMethod,
                        EntityType entityType,
                        Type returnType) {
    this.id = id;
    this.requestInfo = requestInfo;
    this.httpMethod = httpMethod;
    this.entityType = entityType;
    this.textTemplate = new TextTemplate(requestInfo.getUrl());
    this.returnType = returnType;

    final int i = requestInfo.getUrl().indexOf('?');
    if (i < 0 || i == requestInfo.getUrl().length() - 1) {
      this.queryStringParams = Collections.emptyMap();
      this.pureUrlTemplate = textTemplate;
    } else {
      final Map<String, String> params = Maps.newHashMap();

      final String queryString = requestInfo.getUrl().substring(i + 1);

      for (String pair : SEPERATE_SPLITTER.split(queryString)) {
        final List<String> param = PARAM_SPLITTER.splitToList(pair);
        params.put(param.get(0), param.get(1));
      }

      this.queryStringParams = Collections.unmodifiableMap(params);

      this.pureUrlTemplate = new TextTemplate(requestInfo.getUrl().substring(0, i));
    }
  }

  public String getId() {
    return id;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public EntityType getEntityType() {
    return entityType;
  }

  public Type getReturnType() {
    return returnType;
  }

  public RequestInfo getRequestInfo() {
    return requestInfo;
  }

  public Map<String, Object> resolveParams(Object parameterObject) {
    if (parameterObject == null) {
      return Collections.emptyMap();
    }
    final ObjectTraversal objectTraversal = ObjectTraversal.wrap(parameterObject);

    final Map<String, Object> params = textTemplate.params(objectTraversal);

    for (Map.Entry<String, String> en : queryStringParams.entrySet()) {
      if (!en.getValue().startsWith("#{")) {
        params.put(en.getKey(), en.getValue());
      }
    }
    return params;
  }

  public String rendUrl(Map<String, Object> params) {
    return textTemplate.rend(params).toString();
//    try {
//      return URLEncoder.encode(textTemplate.rend(params).toString(), urlEncoding);
//    } catch (UnsupportedEncodingException e) {
//      throw new RuntimeException(e);
//    }
  }

  public String rendPureUrl(Map<String, Object> params) {
    return pureUrlTemplate.rend(params).toString();
//    try {
//      return URLEncoder.encode(pureUrlTemplate.rend(params).toString(), urlEncoding);
//    } catch (UnsupportedEncodingException e) {
//      throw new RuntimeException(e);
//    }
  }

  @Override
  public String toString() {
    return "MappedRequest{" +
        "id='" + id + '\'' +
        ", requestInfo=" + requestInfo +
        ", httpMethod=" + httpMethod +
        ", entityType=" + entityType +
        ", textTemplate=" + textTemplate +
        ", returnType=" + returnType +
        ", queryStringParams=" + queryStringParams +
        ", pureUrlTemplate=" + pureUrlTemplate +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MappedRequest that = (MappedRequest) o;
    return Objects.equal(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public static final class MappedRequestBuilder {
    private String id;
    private HttpMethod httpMethod;
    private EntityType entityType;
    private final Type returnType;
    private RequestInfo requestInfo;

    public MappedRequestBuilder(Method method) {
      if (method.getReturnType() != void.class) {
        this.returnType = method.getGenericReturnType();
      } else {
        final Class<?>[] parameterClasses = method.getParameterTypes();
        if (parameterClasses != null && parameterClasses.length > 0
            && parameterClasses[parameterClasses.length - 1] == ResponseCallback.class) {
          final Type[] parameterTypes = method.getGenericParameterTypes();
          final Type callbackType = parameterTypes[parameterTypes.length - 1];
          if (callbackType instanceof ParameterizedType) {
            final Type type = ((ParameterizedType) callbackType).getActualTypeArguments()[0];
            this.returnType = type;
          } else {
            this.returnType = Map.class;
          }
        } else {
          this.returnType = void.class;
        }

      }
    }

    public MappedRequestBuilder setId(String id) {
      this.id = id;
      return this;
    }

    public MappedRequestBuilder setHttpMethod(HttpMethod httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    public MappedRequestBuilder setEntityType(EntityType entityType) {
      this.entityType = entityType;
      return this;
    }

    public MappedRequestBuilder setRequestInfo(RequestInfo requestInfo) {
      this.requestInfo = requestInfo;
      return this;
    }

    public MappedRequest build() {
      if (httpMethod == null) {
        httpMethod = HttpMethod.GET;
      }
      return new MappedRequest(id, requestInfo, httpMethod, entityType, returnType);
    }
  }
}
