package cn.erika.utils.io;

import cn.erika.socket.model.pto.FileInfo;
import cn.erika.utils.security.MessageDigestUtils;

import java.io.*;

public class FileUtils {
    public static final String SYS_TEMP_DIR = System.getProperty("java.io.tmpdir");
    public static final String SYS_FILE_SEPARATOR = System.getProperty("file.separator");
    private static final int DEFAULT_BLOCK_SIZE = 4 * 1024;

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        copyStream(in, out, DEFAULT_BLOCK_SIZE);
    }

    public static void copyStream(InputStream in, OutputStream out, int blockSize) throws IOException {
        int len = 0;
        byte[] data = new byte[blockSize];
        while ((len = in.read(data)) > -1) {
            out.write(data, 0, len);
        }
    }

    public static void createFile(File file) throws IOException {
        String path = file.getAbsolutePath();
        String parentPath = path.substring(0, path.lastIndexOf(SYS_FILE_SEPARATOR));

        if (file.exists() && !file.delete()) {
            throw new IOException("文件无法删除: " + file.getAbsolutePath());
        }
        File parent = new File(parentPath);
        if (parent.exists() && !parent.isDirectory()) {
            throw new IOException("无法创建目标文件的上级目录: " + file.getParentFile().getAbsolutePath());
        }
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("无法创建文件夹: " + file.getParentFile().getAbsolutePath());
        }
        if (!file.createNewFile()) {
            throw new IOException("无法创建文件: " + file.getAbsolutePath());
        }
    }

    public static File writeFile(String filename, byte[] data) throws IOException {
        File file = new File(filename);
        createFile(file);
        try (FileOutputStream writer = new FileOutputStream(file);
             ByteArrayInputStream reader = new ByteArrayInputStream(data)) {
            copyStream(reader, writer);
            return file;
        }
    }

    public static byte[] readFile(String filename) throws IOException {
        File file = new File(filename);
        try (FileInputStream reader = new FileInputStream(file);
             ByteArrayOutputStream writer = new ByteArrayOutputStream()) {
            copyStream(reader, writer);
            return writer.toByteArray();
        }
    }

    public static FileInfo getFileInfo(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + file.getAbsolutePath());
        }
        long checkCode = MessageDigestUtils.crc32Sum(file);
        FileInfo info = new FileInfo();
        info.setFilename(file.getName());
        info.setFileLength(file.length());
        info.setCheckCode(checkCode);
        return info;
    }

    public static FileInfo getFileInfo(String filename) throws IOException {
        File file = new File(filename);
        return getFileInfo(file);
    }
}
