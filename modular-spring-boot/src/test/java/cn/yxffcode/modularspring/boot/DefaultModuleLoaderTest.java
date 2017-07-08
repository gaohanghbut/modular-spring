package cn.yxffcode.modularspring.boot;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author gaohang on 6/30/17.
 */
public class DefaultModuleLoaderTest {
  @Test
  public void testClasspath() throws Exception {
    final Enumeration<URL> resources = DefaultModuleLoaderTest.class.getClassLoader().getResources("META-INF/");
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      final String file = url.getFile();
      final File f = new File(file);
      if (!f.isDirectory()) {
        continue;
      }
      final String[] rses = f.list();
      for (String rse : rses) {
        System.out.println(rse);
      }
    }
  }

}