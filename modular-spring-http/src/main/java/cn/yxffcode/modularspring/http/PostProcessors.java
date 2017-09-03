package cn.yxffcode.modularspring.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * @author gaohang on 8/6/17.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PostProcessors {
  Class<? extends RequestPostProcessor>[] value() default DoNothingRequestPostProcessor.class;

  final class DoNothingRequestPostProcessor implements RequestPostProcessor {

    @Override
    public boolean postProcessRequest(HttpUriRequest request, MappedRequest mr, Map<String, Object> params) {
      return true;
    }

    @Override
    public void postProcessResponse(HttpResponse response, MappedRequest mr) {
    }
  }
}
