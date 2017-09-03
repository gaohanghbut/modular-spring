package cn.yxffcode.modularspring.http;

import cn.yxffcode.modularspring.http.cfg.Configuration;
import cn.yxffcode.modularspring.http.http.HttpClientFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class HttpMapperScanner extends ClassPathBeanDefinitionScanner {

  private Class<? extends Annotation> annotationClass;

  private final HttpClientFactory httpClientFactory;
  private final List<RequestPostProcessor> requestPostProcessors;
  private final ResponseHandler defaultResponseHandler;
  private Configuration configuration;

  public HttpMapperScanner(BeanDefinitionRegistry registry,
                           HttpClientFactory httpClientFactory,
                           List<RequestPostProcessor> requestPostProcessors,
                           ResponseHandler defaultResponseHandler,
                           Class<? extends Annotation> annotation) {
    super(registry, false);
    this.httpClientFactory = httpClientFactory;
    this.requestPostProcessors = requestPostProcessors;
    this.defaultResponseHandler = defaultResponseHandler;
    this.annotationClass = annotation;
  }

  public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
    this.annotationClass = annotationClass;
  }

  public void registerFilters() {
    boolean acceptAllInterfaces = true;

    if (this.annotationClass != null) {
      addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
      acceptAllInterfaces = false;
    }

    if (acceptAllInterfaces) {
      addIncludeFilter(new TypeFilter() {
        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
          return true;
        }
      });
    }

    addExcludeFilter(new TypeFilter() {
      @Override
      public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        String className = metadataReader.getClassMetadata().getClassName();
        return className.endsWith("package-info");
      }
    });
  }

  @Override
  public Set<BeanDefinitionHolder> doScan(String... basePackages) {
    Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

    if (beanDefinitions.isEmpty()) {
      logger.warn("No Http mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
    } else {
      processBeanDefinitions(beanDefinitions);
    }

    return beanDefinitions;
  }

  private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
    final Configuration.ConfigurationBuilder builder = Configuration.newBuilder();
    if (requestPostProcessors != null && requestPostProcessors.size() > 0) {
      for (RequestPostProcessor requestPostProcessor : requestPostProcessors) {
        builder.addCommonRequestPostProcessor(requestPostProcessor);
      }
    }
    builder.setHttpClientFactory(httpClientFactory);
    builder.setDefaultResponseHandler(defaultResponseHandler);
    try {
      for (BeanDefinitionHolder holder : beanDefinitions) {
        GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
        final Class<?> beanClass = Class.forName(definition.getBeanClassName());
        builder.parse(beanClass);
        definition.setBeanClass(HttpMapperFactoryBean.class);
        definition.getConstructorArgumentValues().addIndexedArgumentValue(0, beanClass);
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    this.configuration = builder.build();
    for (BeanDefinitionHolder beanDefinition : beanDefinitions) {
      final BeanDefinition definition = beanDefinition.getBeanDefinition();
      definition.getConstructorArgumentValues().addIndexedArgumentValue(1, configuration);
    }
  }

  @Override
  protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
    return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
  }

  public Configuration getConfiguration() {
    return configuration;
  }
}
