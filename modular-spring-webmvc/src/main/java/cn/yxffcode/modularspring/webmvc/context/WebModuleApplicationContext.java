package cn.yxffcode.modularspring.webmvc.context;

import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author gaohang on 7/9/17.
 */
public interface WebModuleApplicationContext extends ModuleApplicationContext, WebApplicationContext {

  @Override
  String getModuleName();

  @Override
  ServletContext getServletContext();
}
