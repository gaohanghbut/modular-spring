package cn.yxffcode.modularspring.http.text;

/**
 * @author gaohang on 8/16/16.
 */
class Segment {
  private final String text;
  private final boolean variable;

  Segment(String text, boolean variable) {
    this.text = text;
    this.variable = variable;
  }

  public String getText() {
    return text;
  }

  public boolean isVariable() {
    return variable;
  }

  @Override
  public String toString() {
    return "Segment{" +
        "text='" + text + '\'' +
        ", variable=" + variable +
        '}';
  }
}
