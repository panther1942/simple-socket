package cn.erika.utils.log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 * 输出日志到文件
 * 如果日志文件存在将在末尾追加日志 不会滚动保存
 */
public class FileLogger implements LogPrinter {
    private Charset charset;
    private String logDir;
    private String logName;

    /**
     * 指定日志目录,日志文件名和编码字符集
     *
     * @param logDir  日志目录
     * @param logName 日志文件名
     * @param charset 编码字符集
     */
    public FileLogger(String logDir, String logName, Charset charset) {
        this.logDir = logDir;
        this.logName = logName;
        this.charset = charset;
    }

    /**
     * 指定日志目录和日志文件名
     * 未指定字符集将使用系统默认字符集 将读取jvm的file.encoding属性
     *
     * @param logDir  日志目录
     * @param logName 日志文件名
     */
    public FileLogger(String logDir, String logName) {
        this.logDir = logDir;
        this.logName = logName;
        this.charset = Charset.forName(System.getProperty("file.encoding"));
    }

    /**
     * 输出日志到文件
     * 将按日志的等级分文件输出 不打算搞彩色日志 这玩意应该是谁读取谁渲染
     *
     * @param level 日志等级
     * @param line  输出的信息
     * @throws IOException 如果尝试创建日志目录及文件的过程在出现错误将抛出该异常 例如没有写权限 目标文件为目录等错误
     */
    @Override
    public synchronized void print(LogLevel level, String line) throws IOException {
        int targetLevel = level.getValue();
        createDirectory(new File(logDir));
        for (int i = 0; i <= targetLevel; i++) {
            level = LogLevel.getByValue(i);
            if (level != null) {
                File file = new File(logDir + logName + "-" + level.getName().toLowerCase() + ".log");
                createFile(file);
                try (RandomAccessFile out = new RandomAccessFile(file, "rw")) {
                    out.seek(file.length());
                    out.write(line.getBytes(charset));
                    out.write(System.lineSeparator().getBytes(charset));
                }
            }
        }
    }

    // 创建日志目录的方法
    private void createDirectory(File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("无法创建目录: " + directory.getAbsolutePath());
        }
        if (!directory.isDirectory()) {
            throw new IOException("不是一个目录: " + directory.getAbsolutePath());
        }
        if (!directory.canWrite()) {
            throw new IOException("目录不可写: " + directory.getAbsolutePath());
        }
    }

    // 创建日志文件的方法
    private void createFile(File file) throws IOException {
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("无法创建文件: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IOException("不是一个文件: " + file.getAbsolutePath());
        }
        if (!file.canWrite()) {
            throw new IOException("文件不可写: " + file.getAbsolutePath());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FileLogger)) {
            return false;
        }
        FileLogger that = (FileLogger) o;
        if (this.logDir.equals(that.logName) && this.logName.equals(that.logName)) {
            return true;
        }
        return false;
    }
}
