package cn.erika.util.log;

import cn.erika.config.GlobalSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileLogger implements LogPrinter {
    private Charset charset = GlobalSettings.charset;
    private String logDir;
    private String logName;

    public FileLogger(String logDir, String logName) {
        this.logDir = logDir;
        this.logName = logName;
    }

    @Override
    public void print(LogLevel level, String line) {
        try {
            int targetLevel = level.getValue();
            createDirectory(new File(logDir));
            for (int i = 0; i <= targetLevel; i++) {
                level = LogLevel.getByValue(i);
                if (level != null) {
                    File file = new File(logDir + logName + "-" + level.getName().toLowerCase() + ".log");
                    createFile(file);
                    try (FileOutputStream out = new FileOutputStream(file, true)) {
                        out.write(line.getBytes(charset));
                        out.write(System.lineSeparator().getBytes(charset));
                        out.flush();
                    }
                }
            }
        } catch (IOException e) {
            LoggerFactory.unregister(this);
        }
    }

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

    public void createFile(File file) throws IOException {
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
}
