package cn.yxffcode.modularspring.http;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Future;

/**
 * @author gaohang on 8/11/17.
 */
public class FastJsonResponseHandler extends ToStringResponseHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(FastJsonResponseHandler.class);
  @Override
  public Object handle(MappedRequest request, HttpResponse response) {
    final String text = (String) super.handle(request, response);
    LOGGER.debug("request result: request={}, result={}", request, text);
    final Type returnType = request.getReturnType();

    if (returnType instanceof Class) {
      return JSON.parseObject(text, (Class<? extends Object>) returnType);
    }

    final ParameterizedType parameterizedType = (ParameterizedType) returnType;

    if (parameterizedType.getRawType() == Future.class) {
      final Type type = parameterizedType.getActualTypeArguments()[0];

      if (type instanceof Class) {
        return JSON.parseObject(text, (Class<? extends Object>) returnType);
      }

      return JSON.parseObject(text, type);
    }

    return JSON.parseObject(text, returnType);
  }
}
