package cn.yxffcode.modularspring.webmvc.view;

import org.springframework.web.servlet.view.AbstractUrlBasedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author gaohang on 10/8/17.
 */
class ModularResourceView extends AbstractUrlBasedView {
  public ModularResourceView() {
  }

  public ModularResourceView(String url) {
    super(url);
  }

  @Override
  protected void renderMergedOutputModel(Map<String, Object> model,
                                         HttpServletRequest request,
                                         HttpServletResponse response) throws Exception {

  }
}
