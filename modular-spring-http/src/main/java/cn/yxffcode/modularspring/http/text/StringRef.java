package cn.yxffcode.modularspring.http.text;

import com.google.common.collect.ImmutableList;

import java.util.AbstractList;

/**
 * @author gaohang on 8/16/16.
 */
public class StringRef {
  private final ImmutableList<String> refs;
  private final int length;

  public StringRef(final String first, final String... others) {
    this(ImmutableList.copyOf(new AbstractList<String>() {
      @Override
      public String get(int index) {
        if (index == 0) {
          return first;
        }
        return others[index - 1];
      }

      @Override
      public int size() {
        return others.length + 1;
      }
    }));
  }

  public StringRef(Iterable<String> values) {
    this.refs = ImmutableList.copyOf(values);
    int len = 0;
    for (String ref : this.refs) {
      len += ref.length();
    }
    this.length = len;
  }

  public int length() {
    return length;
  }

  public char charAt(final int index) {
    if (index < 0 || index >= length()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    int idx = index;
    for (String ref : refs) {
      if (idx < ref.length()) {
        return ref.charAt(idx);
      }
      idx -= ref.length();
    }
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (String ref : refs) {
      sb.append(ref);
    }
    return sb.toString();
  }
}
