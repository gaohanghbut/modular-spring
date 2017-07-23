package cn.yxffcode.modularspring.webmvc.view;

import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author gaohang on 7/23/17.
 */
public class ModuleResourceViewResolver extends InternalResourceViewResolver implements InitializingBean {

  /**
   * 前端页面文件(jsp/html)的默认目录
   */
  private static final String DEFAULT_VIEW_BASE_DIR = "view";

  private String viewBaseDir;
  /**
   * 表示此ViewResolver是否是针对单个模块
   */
  private boolean isModuleViewResolver;

  @Override
  public void setPrefix(String prefix) {
    if (!isModuleViewResolver || isBlank(viewBaseDir)) {
      super.setPrefix(prefix);
      return;
    }
    super.setPrefix(viewBaseDir + prefix);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.isModuleViewResolver = getApplicationContext() instanceof ModuleApplicationContext;
  }

  public void setViewBaseDir(String viewBaseDir) {
    if (isBlank(viewBaseDir)) {
      this.viewBaseDir = DEFAULT_VIEW_BASE_DIR;
      return;
    }
    String dir = viewBaseDir;
    if (dir.startsWith("/")) {
      dir = dir.substring(1);
    }
    if (isBlank(dir)) {
      return;
    }
    if (dir.endsWith("/")) {
      dir = dir.substring(0, dir.length() - 1);
    }
    this.viewBaseDir = dir;
  }
}
