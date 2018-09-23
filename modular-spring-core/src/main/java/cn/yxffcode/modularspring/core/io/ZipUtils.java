package cn.yxffcode.modularspring.core.io;

import com.google.common.io.Closer;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.*;

/**
 * @author gaohang on 2/15/18.
 */
public abstract class ZipUtils {
  private ZipUtils() {
  }

  /**
   * 把zip文件解压到指定的文件夹
   *
   * @param zipFilePath zip文件路径, 如 "D:/test/aa.zip"
   * @param saveFileDir 解压后的文件存放路径, 如"D:/test/"
   */
  public static void decompressZip(final String zipFilePath, final String saveFileDir) throws IOException {
    File file = new File(zipFilePath);
    if (!file.exists()) {
      throw new FileNotFoundException(zipFilePath);
    }

    mkdirs(saveFileDir);

    //can read Zip archives
    try (final Closer closer = Closer.create()) {
      final InputStream is = closer.register(new FileInputStream(file));
      final ZipArchiveInputStream zais = closer.register(new ZipArchiveInputStream(is));
      ArchiveEntry archiveEntry;
      //把zip包中的每个文件读取出来
      //然后把文件写到指定的文件夹
      while ((archiveEntry = zais.getNextEntry()) != null) {
        if (archiveEntry.isDirectory()) {
          final File dir = new File(saveFileDir, archiveEntry.getName());
          dir.mkdirs();
          continue;
        }
        //获取文件名
        //构造解压出来的文件存放路径
        final File entryFile = new File(saveFileDir + archiveEntry.getName());
        if (!entryFile.exists()) {
          entryFile.createNewFile();
        }
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(entryFile))) {
          //把解压出来的文件写到指定路径
          final byte[] content = new byte[100];
          do {
            final int count = zais.read(content);

            if (count < 0) {
              break;
            }
            os.write(content, 0, count);
          } while (true);
        }
      }
    }
  }

  private static void mkdirs(String saveFileDir) {
    final File target = new File(saveFileDir);
    if (target.exists()) {
      target.delete();
    }
    target.mkdirs();
  }
}
