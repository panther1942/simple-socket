package cn.erika.cli.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class KeyboardReader {
    private static Logger log = LoggerFactory.getLogger(KeyboardReader.class);
    private static final String LOCAL_CODE = System.getProperty("file.encoding");

    private static KeyboardReader readerKB;
    // 输入对象，读取键盘属于读取字符
    private BufferedReader reader;

    private KeyboardReader() {
        try {
            reader = new BufferedReader(new InputStreamReader(System.in, LOCAL_CODE));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static KeyboardReader getInstance() {
        if (readerKB == null) {
            readerKB = new KeyboardReader();
        }
        return readerKB;
    }

    public String read() {
        if (reader != null) {
            String line;
            try {
                if ((line = reader.readLine()) != null) {
                    return line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            log.error("Failed to get inputStream");
        }
        return null;
    }

    public String read(String tip) {
        log.info(tip);
        return read();
    }
}
