package cn.yxffcode.modularspring.boot;

import cn.yxffcode.modularspring.boot.listener.ApplicationStartupCallback;
import cn.yxffcode.modularspring.boot.listener.ModuleLoadListener;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Map;

/**
 * @author gaohang on 6/24/17.
 */
public interface ModuleLoader {
  Map<ModuleConfig, ModuleApplicationContext> load(ClassLoader classLoader) throws IOException;

  void addModuleLoadListener(ModuleLoadListener moduleLoadListener);
}
