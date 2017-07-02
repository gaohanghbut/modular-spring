package cn.yxffcode.modularspring.core.io;

import org.springframework.core.io.AbstractResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author gaohang on 7/2/17.
 */
public class JarEntryResource extends AbstractResource {

  private final String path;

  public JarEntryResource(String path) {
    this.path = path;
  }

  @Override
  public String getDescription() {
    return path;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    final InputStream inputStream = JarEntryReader.getInputStream(path);
    if (inputStream == null) {
      throw new IOException(path + " is not exists");
    }
    return inputStream;
  }
}
