package cn.erika.utils.io.compress.file;

import cn.erika.utils.io.FileUtils;
import cn.erika.utils.exception.CompressException;
import cn.erika.utils.io.compress.CompressUtils;
import cn.erika.utils.io.compress.stream.GZIP;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZIP implements FileCompress {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    public static final String NAME = "ZIP";
    public static final int CODE = 0x11;

    static {
        CompressUtils.register(new GZIP());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getCode() {
        return CODE;
    }

    @Override
    public void compress(File src, File dest) throws IOException, CompressException {
        FileUtils.createFile(dest);
        try (FileInputStream fileReader = new FileInputStream(src);
             FileOutputStream fileWriter = new FileOutputStream(dest);
             ZipOutputStream zipWriter = new ZipOutputStream(fileWriter)) {
            zipWriter.putNextEntry(new ZipEntry(src.getName()));
            zipWriter.setComment(src.getName());
            FileUtils.copyStream(fileReader, zipWriter, 4 * 1024);
        } catch (IOException e) {
            throw new CompressException(e.getMessage(), e);
        }
    }

    @Override
    public void compressDir(File directory, File file) throws IOException, CompressException {
        if (!directory.isDirectory()) {
            throw new IOException("目标不是目录");
        }
        compressFiles(directory.listFiles(), file);
    }

    @Override
    public void compressFiles(File[] files, File archiveFile) throws IOException, CompressException {
        FileUtils.createFile(archiveFile);
        try (FileOutputStream fileWriter = new FileOutputStream(archiveFile);
             ZipOutputStream zipWriter = new ZipOutputStream(fileWriter)) {
            for (File file : files) {
                try (FileInputStream fileReader = new FileInputStream(file)) {
                    zipWriter.putNextEntry(new ZipEntry(file.getName()));
                    zipWriter.setComment(file.getName());
                    FileUtils.copyStream(fileReader, zipWriter, 4 * 1024);
                } catch (FileNotFoundException e) {
                    log.error("文件不存在 跳过: " + file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new CompressException(e.getMessage(), e);
        }
    }

    @Override
    public void decompress(File archiveFile, File directory) throws CompressException {
        try (ZipFile zipFile = new ZipFile(archiveFile);
             FileInputStream zipStream = new FileInputStream(archiveFile);
             ZipInputStream zipReader = new ZipInputStream(zipStream)) {
            ZipEntry entry = null;
            while ((entry = zipReader.getNextEntry()) != null) {
                log.debug("Decompress file: " + entry.getName());
                File targetFile = new File(directory.getAbsolutePath()
                        + FileUtils.SYS_FILE_SEPARATOR
                        + entry.getName());
                try {
                    FileUtils.createFile(targetFile);
                } catch (IOException e) {
                    log.error("跳过文件: " + targetFile.getAbsolutePath()
                            + "\n原因: " + e.getMessage());
                    continue;
                }
                try (FileOutputStream fileWriter = new FileOutputStream(targetFile);
                     InputStream entryReader = zipFile.getInputStream(entry)) {
                    FileUtils.copyStream(entryReader, fileWriter, 4 * 1024);
                }
            }
        } catch (IOException e) {
            throw new CompressException(e.getMessage(), e);
        }
    }
}
