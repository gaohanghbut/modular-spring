package cn.yxffcode.modularspring.http;

import cn.yxffcode.modularspring.http.http.HttpClientFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author gaohang on 8/12/17.
 */
public class HttpMapperAutoConfigurer implements BeanDefinitionRegistryPostProcessor {

  private HttpClientFactory httpClientFactory;
  private List<RequestPostProcessor> commonRequestPostProcessors;
  private ResponseHandler defaultResponseHandler;
  private String[] basePackages;
  private Class<? extends Annotation> annotation;

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    final HttpMapperScanner scanner = new HttpMapperScanner(registry, httpClientFactory,
        commonRequestPostProcessors, defaultResponseHandler, annotation);
    scanner.registerFilters();
    scanner.doScan(basePackages);
    scanner.getConfiguration();
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
  }

  public HttpClientFactory getHttpClientFactory() {
    return httpClientFactory;
  }

  public void setHttpClientFactory(HttpClientFactory httpClientFactory) {
    this.httpClientFactory = httpClientFactory;
  }

  public List<RequestPostProcessor> getCommonRequestPostProcessors() {
    return commonRequestPostProcessors;
  }

  public void setCommonRequestPostProcessors(List<RequestPostProcessor> commonRequestPostProcessors) {
    this.commonRequestPostProcessors = commonRequestPostProcessors;
  }

  public ResponseHandler getDefaultResponseHandler() {
    return defaultResponseHandler;
  }

  public void setDefaultResponseHandler(ResponseHandler defaultResponseHandler) {
    this.defaultResponseHandler = defaultResponseHandler;
  }

  public String[] getBasePackages() {
    return basePackages;
  }

  public void setBasePackages(String[] basePackages) {
    this.basePackages = basePackages;
  }

  public Class<? extends Annotation> getAnnotation() {
    return annotation;
  }

  public void setAnnotation(Class<? extends Annotation> annotation) {
    this.annotation = annotation;
  }
}
