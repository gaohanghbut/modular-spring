package cn.yxffcode.modularspring.core.io;

import java.io.File;
import java.util.LinkedList;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 对文件系统中一个目录的包装
 *
 * @author gaohang on 15/9/30.
 */
public final class DirectoryWrapper {

  private final File dir;

  private DirectoryWrapper(final File dir) {
    checkArgument(dir.isDirectory());
    this.dir = dir;
  }

  public static DirectoryWrapper wrap(File dir) {
    checkNotNull(dir);
    return new DirectoryWrapper(dir);
  }

  /**
   * 删除整个目录下所有的文件，包括此目录
   *
   * @return 删除是否成功，如果至少有一个文件或者目录删除失败，则返回false，否则返回true
   */
  public boolean delete() {

    //使用深度优先搜索删除目录中的所有文件和目录
    LinkedList<File> stack = new LinkedList<>();
    stack.add(dir);
    boolean success = true;//只要有一个文件或者目录删除失败，则为false
    while (!stack.isEmpty()) {
      File first = stack.removeFirst();
      if (first.isFile()) {
        success = success && first.delete();
      } else if (first.isDirectory()) {
        final File[] files = first.listFiles();
        if (files == null || files.length == 0) {
          success = success && first.delete();
          continue;
        }
        for (File f : files) {
          stack.addFirst(f);
        }
        stack.addLast(first);//将目录再次加入栈，在目录中的文件被删除后，目录也会被删除
      }
    }
    return success;
  }
}
