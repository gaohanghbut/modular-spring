package cn.yxffcode.modularspring.webmvc.view;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author gaohang on 7/23/17.
 */
public class ModuleResourceViewResolver extends InternalResourceViewResolver {

  /**
   * 前端页面文件(jsp/html)的默认目录
   */
  private static final String DEFAULT_VIEW_BASE_DIR = "view";

  private String viewBaseDir;

  public ModuleResourceViewResolver() {
    this(DEFAULT_VIEW_BASE_DIR);
  }

  public ModuleResourceViewResolver(String viewBaseDir) {
    setViewBaseDir(viewBaseDir);
    setPrefix(null);
  }

  @Override
  public void setPrefix(String prefix) {
    if (StringUtils.isBlank(prefix)) {
      super.setPrefix(viewBaseDir + '/');
      return;
    }
    super.setPrefix(viewBaseDir + '/' + prefix);
  }

  private void setViewBaseDir(String viewBaseDir) {
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
