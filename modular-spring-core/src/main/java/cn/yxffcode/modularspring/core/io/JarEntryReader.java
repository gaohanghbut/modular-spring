package cn.yxffcode.modularspring.core.io;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author gaohang on 7/2/17.
 */
public final class JarEntryReader {
  private static final Splitter JAR_FILE_SPLITTER = Splitter.on("!/").trimResults();

  private JarEntryReader() {
  }

  public static String toString(final String jarEntryPath) throws IOException {
    checkArgument(!Strings.isNullOrEmpty(jarEntryPath));

    String jarFilePath = jarEntryPath;
    if (jarEntryPath.startsWith("jar:")) {
      jarFilePath = jarEntryPath.substring("jar:".length());
    }

    final List<String> jarAndFile = JAR_FILE_SPLITTER.splitToList(jarFilePath);
    final String jarName = jarAndFile.get(0);
    final String filePath = jarAndFile.get(1);

    return toString(jarName, filePath);
  }

  public static String toString(final String jarName, final String filePath) throws IOException {
    checkArgument(!Strings.isNullOrEmpty(jarName));
    checkArgument(!Strings.isNullOrEmpty(filePath));

    try (final Closer closer = Closer.create()) {
      final JarArchiveInputStream in = closer.register(new JarArchiveInputStream(new FileInputStream(jarName)));
      JarArchiveEntry jarEntry = in.getNextJarEntry();
      while (jarEntry != null) {
        if (!jarEntry.isDirectory()) {
          if (StringUtils.equals(jarEntry.getName(), filePath)) {
            final BufferedReader reader = closer.register(new BufferedReader(new InputStreamReader(in)));
            return CharStreams.toString(reader);
          }
        }
        jarEntry = in.getNextJarEntry();
      }
    }
    return null;
  }

  /**
   * 通过jarEntry的路径获取输入流
   */
  public static InputStream getInputStream(final String jarEntryPath) throws IOException {
    checkArgument(!Strings.isNullOrEmpty(jarEntryPath));

    String jarFilePath = jarEntryPath;
    if (jarEntryPath.startsWith("jar:")) {
      jarFilePath = jarEntryPath.substring("jar:".length());
    }

    final List<String> jarAndFile = JAR_FILE_SPLITTER.splitToList(jarFilePath);
    final String jarName = jarAndFile.get(0);
    final String filePath = jarAndFile.get(1);

    final JarArchiveInputStream in = new JarArchiveInputStream(new FileInputStream(jarName));
    boolean closeStream = true;
    try {
      JarArchiveEntry jarEntry = in.getNextJarEntry();
      while (jarEntry != null) {
        if (!jarEntry.isDirectory()) {
          if (StringUtils.equals(jarEntry.getName(), filePath)) {
            closeStream = false;
            return in;
          }
        }
        jarEntry = in.getNextJarEntry();
      }
      return null;
    } finally {
      if (closeStream) {
        in.close();
      }
    }

  }

}
