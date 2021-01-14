package cn.erika.utils.io;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.utils.exception.UnsupportedAlgorithmException;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.security.MessageDigestAlgorithm;
import cn.erika.utils.security.MessageDigestUtils;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.string.Base64Utils;
import cn.erika.utils.string.StringUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.List;

public class FileUtils {
    public static final String SYS_TEMP_DIR = System.getProperty("java.io.tmpdir");
    public static final String SYS_FILE_SEPARATOR = System.getProperty("file.separator");

    private static final int DEFAULT_BLOCK_SIZE = 4 * 1024;
    private static int blockSize = GlobalSettings.fileTransBlock;
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);
    private static DecimalFormat df = new DecimalFormat("0.00%");

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
        if (!file.canRead()) {
            throw new IOException("文件不可读");
        }
        try {
            MessageDigestAlgorithm algorithm = SecurityUtils.getMessageDigestAlgorithmByValue(GlobalSettings.fileSignAlgorithm);
            byte[] sign = MessageDigestUtils.sum(file, algorithm);
            FileInfo info = new FileInfo();
            info.setFilename(file.getName());
            info.setLength(file.length());
            info.setSign(StringUtils.byte2HexString(sign));
            info.setAlgorithm(algorithm.getValue());
            return info;
        } catch (UnsupportedAlgorithmException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static FileInfo getFileInfo(String filename) throws IOException {
        File file = new File(filename);
        return getFileInfo(file);
    }

    public static boolean checkFile(File file, FileInfo fileInfo) throws IOException, UnsupportedAlgorithmException {
        String sign = fileInfo.getSign();
        String algorithm = fileInfo.getAlgorithm();
        byte[] reSign = MessageDigestUtils.sum(file, SecurityUtils.getMessageDigestAlgorithmByValue(algorithm));
        if (StringUtils.byte2HexString(reSign).equalsIgnoreCase(sign)) {
            return true;
        } else {
            return false;
        }
    }

    public static void sendFile(ISocket socket, String serviceName, String filename, FileInfo fileInfo) throws IOException {
        File file = new File(filename);
        long skip = fileInfo.getPos();
        long partLength = fileInfo.getLength();

        try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
            reader.seek(skip);
            long pos = 0;
            int len;
            byte[] data = new byte[blockSize];

            while ((len = reader.read(data)) > -1) {
                // 如果读取的数据累计超过片段长度 则截取至片段长度
                if (pos + len > partLength) {
                    len = (int) (partLength - pos);
                }
                // 复制为新数组 删除掉空白部分
                byte[] tmp = new byte[len];
                System.arraycopy(data, 0, tmp, 0, len);
                Message msg = new Message(serviceName);
                // 为了避免序列化和反序列化出现错误 数据使用BASE64编码
                msg.add(Constant.FILE_POS, pos);
                msg.add(Constant.BIN, tmp);
                msg.add(Constant.LEN, tmp.length);
                pos += len;
                log.info("进度: " + df.format(pos / (double) partLength));
                if (!socket.send(msg)) {
                    throw new IOException("输出流中断");
                }
                if (pos >= partLength) {
                    break;
                }
            }
        }
    }

    public static void receiveFile(ISocket socket, String filename, long partLen, long pos, int len, byte[] data) throws IOException {
        File file = new File(filename);
        try (RandomAccessFile out = new RandomAccessFile(file, "rwd")) {
            log.info("当前进度: " + df.format((pos + len) / (double) partLen) + ": " + file.getName());
            out.seek(pos);
            out.write(data, 0, len);
        } catch (IOException e) {
            Message error = new Message(Constant.SRV_TEXT);
            error.add(Constant.TEXT, e.getMessage());
            socket.send(error);
            socket.close();
            throw new IOException(e.getMessage(), e);
        }
    }

    public static boolean checkFilePart(ISocket socket, FileInfo fileInfo) throws IOException, UnsupportedAlgorithmException {
        File file = new File(fileInfo.getFilename());
        String filepath = file.getAbsolutePath();
        log.info("文件位置: " + filepath);
        ISocket parent = socket.get(Constant.PARENT_SOCKET);
        long crc = MessageDigestUtils.crc32Sum(file);
        if (fileInfo.getCrc() == crc) {
            log.info("数据完整: " + filepath);
            parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收完成: " + filepath));
            return true;
        } else {
            log.warn("数据不完整: " + filepath);
            parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收失败: " + filepath));
            return false;
        }
    }

    public static void mergeFile(File destFile, List<FileInfo> fileInfoList) throws IOException {
        try (RandomAccessFile writer = new RandomAccessFile(destFile, "rw")) {
            for (FileInfo info : fileInfoList) {
                File file = new File(info.getFilename());
                long pos = info.getPos();
                writer.seek(pos);
                try (FileInputStream reader = new FileInputStream(file)) {
                    int len = 0;
                    byte[] data = new byte[4 * 1024 * 1024];
                    while ((len = reader.read(data)) > -1) {
                        writer.write(data, 0, len);
                    }
                }
                file.delete();
            }
        }
    }
}
