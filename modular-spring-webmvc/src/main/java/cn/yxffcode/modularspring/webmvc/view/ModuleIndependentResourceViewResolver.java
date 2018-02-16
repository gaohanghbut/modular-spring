package cn.yxffcode.modularspring.webmvc.view;

import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

/**
 * @author gaohang on 10/8/17.
 */
public class ModuleIndependentResourceViewResolver extends UrlBasedViewResolver {
  @Override
  protected AbstractUrlBasedView buildView(String viewName) throws Exception {
    return super.buildView(viewName);
  }

}
