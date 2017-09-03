package cn.yxffcode.modularspring.http;

/**
 * @author gaohang on 8/13/17.
 */
public class RequestInfo {
  private final String url;
  private final String attach;
  private final String urlEncoding;
  private final long timeout;

  public RequestInfo(Request request) {
    final String rawUrl = request.value();
    final int idx = rawUrl.lastIndexOf('#');
    if (idx < 0) {
      this.url = rawUrl;
      this.attach = "";
    } else if (idx < rawUrl.length() - 1 && rawUrl.charAt(idx + 1) != '{') {
      this.url = rawUrl.substring(0, idx);
      if (idx != rawUrl.length() - 1) {
        this.attach = rawUrl.substring(idx + 1);
      } else {
        this.attach = "";
      }
    } else {
      this.url = rawUrl;
      this.attach = "";
    }
    this.urlEncoding = request.urlCharset();
    this.timeout = request.timeout();
  }

  public String getUrl() {
    return url;
  }

  public String getAttach() {
    return attach;
  }

  public String getUrlEncoding() {
    return urlEncoding;
  }

  public long getTimeout() {
    return timeout;
  }
}
