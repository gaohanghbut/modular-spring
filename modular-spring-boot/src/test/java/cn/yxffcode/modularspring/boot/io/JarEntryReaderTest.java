package cn.yxffcode.modularspring.boot.io;

import cn.yxffcode.modularspring.core.io.JarEntryReader;
import org.junit.Test;

/**
 * @author gaohang on 7/2/17.
 */
public class JarEntryReaderTest {
  @Test
  public void testToString() throws Exception {

    final String s = JarEntryReader.toString("jar:/Users/gaohang/.m2/repository/cn/yxffcode/modular-spring-core/1.0-SNAPSHOT/modular-spring-core-1.0-SNAPSHOT.jar!/META-INF/module.json");
    System.out.println(s);
  }

}