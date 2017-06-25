package cn.yxffcode.modularspring.boot;

import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Map;

/**
 * @author gaohang on 6/24/17.
 */
public interface ModuleLoader {
  Map<ModuleConfig, ApplicationContext> load(ClassLoader classLoader) throws IOException;
}
