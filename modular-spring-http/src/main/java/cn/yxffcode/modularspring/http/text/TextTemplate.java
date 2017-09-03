package cn.yxffcode.modularspring.http.text;

import cn.yxffcode.modularspring.http.reflection.ObjectTraversal;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author gaohang on 8/16/16.
 */
public class TextTemplate {
  private final String text;
  private List<Segment> segments;

  public TextTemplate(String text) {
    this.text = text;
    if (Strings.isNullOrEmpty(text)) {
      this.segments = Collections.emptyList();
      return;
    }
    this.segments = Lists.newArrayList();
    final StringBuilder sb = new StringBuilder();
    for (int i = 0, j = text.length(); i < j; i++) {
      final char c = text.charAt(i);
      if (i >= j - 2) {
        sb.append(c);
        continue;
      }
      if (c == '\\') {
        if (text.charAt(i + 1) == '\\') {
          sb.append('\\');
          ++i;
        } else if (text.charAt(i + 1) == '#' && text.charAt(i + 2) == '{') {
          sb.append('#').append('{');
          i += 2;
        } else {
          sb.append(c);
        }
      } else if (c == '#' && text.charAt(i + 1) == '{') {//variable
        segments.add(new Segment(sb.toString(), false));
        sb.delete(0, sb.length());//clean

        //extract variable name
        for (i += 2; i < j; i++) {
          final char vc = text.charAt(i);
          if (vc == '}') {
            segments.add(new Segment(sb.toString(), true));
            sb.delete(0, sb.length());//clean
            break;
          }
          sb.append(vc);
        }
      } else {
        sb.append(c);
      }
    }
    if (sb.length() > 0) {
      segments.add(new Segment(sb.toString(), false));
    }
  }

  public StringRef rend(final ObjectTraversal context) {
    for (Segment segment : segments) {
      if (segment.isVariable() && context == null) {
        throw new PlaceNotFoundException(segment.getText() + " cannot be found in context:" + context);
      }
    }
    return new StringRef(Iterables.transform(segments, new Function<Segment, String>() {
      @Override
      public String apply(Segment segment) {
        // NOTE:可以支持序列化方式,但是过于复杂,与设计目的不符
        return segment.isVariable() ? String.valueOf(context.get(segment.getText())) : segment.getText();
      }
    }));
  }

  public StringRef rend(final Map<String, ?> context) {
    for (Segment segment : segments) {
      if (segment.isVariable() && (context == null || !context.containsKey(segment.getText()))) {
        throw new PlaceNotFoundException(segment.getText() + " cannot be found in context:" + context);
      }
    }
    return new StringRef(Iterables.transform(segments, new Function<Segment, String>() {
      @Override
      public String apply(Segment segment) {
        // NOTE:可以支持序列化方式,但是过于复杂,与设计目的不符
        return segment.isVariable() ? String.valueOf(context.get(segment.getText())) : segment.getText();
      }
    }));
  }

  public Map<String, Object> params(final ObjectTraversal context) {

    final Map<String, Object> params = Maps.newHashMap();
    for (Segment segment : segments) {
      if (segment.isVariable() && context == null) {
        throw new PlaceNotFoundException(segment.getText() + " cannot be found in context:" + context);
      }

      if (segment.isVariable()) {
        final Object v = context.get(segment.getText());
        params.put(segment.getText(), v);
      }
    }
    return params;
  }

}
