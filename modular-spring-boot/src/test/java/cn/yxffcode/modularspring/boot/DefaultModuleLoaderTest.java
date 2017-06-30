package cn.yxffcode.modularspring.boot;

import com.google.common.reflect.ClassPath;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import static org.junit.Assert.*;

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