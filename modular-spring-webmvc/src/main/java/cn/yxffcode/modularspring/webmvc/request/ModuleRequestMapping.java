package cn.yxffcode.modularspring.webmvc.request;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;

/**
 * @author gaohang on 7/9/17.
 */
class ModuleRequestMapping implements RequestMapping {

  private static final RequestMethod[] EMPTY_REQUEST_METHOD_ARRAY = new RequestMethod[0];

  private final String[] moduleNames;

  ModuleRequestMapping(final String moduleName) {
    moduleNames = new String[]{
        moduleName
    };
  }

  @Override
  public String name() {
    return StringUtils.EMPTY;
  }

  @Override
  public String[] value() {
    return moduleNames;
  }

  @Override
  public String[] path() {
    return moduleNames;
  }

  @Override
  public RequestMethod[] method() {
    return EMPTY_REQUEST_METHOD_ARRAY;
  }

  @Override
  public String[] params() {
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  @Override
  public String[] headers() {
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  @Override
  public String[] consumes() {
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  @Override
  public String[] produces() {
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return RequestMapping.class;
  }
}
