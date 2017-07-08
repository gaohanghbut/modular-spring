package cn.yxffcode.modularspring.boot.spring;

import cn.yxffcode.modularspring.boot.utils.ModuleLoadContextHolder;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;

/**
 * @author gaohang on 7/9/17.
 */
public class ModuleTypeFilter implements TypeFilter {
  @Override
  public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
    final String loadingModulePath = ModuleLoadContextHolder.getLoadingModulePath();
    return !metadataReader.getResource().getURL().toString().contains(loadingModulePath);
  }
}
