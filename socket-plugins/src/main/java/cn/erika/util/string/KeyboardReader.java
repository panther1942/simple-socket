package cn.erika.util.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class KeyboardReader {
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

    public String read() throws IOException {
        if (reader != null) {
            String line;
            if ((line = reader.readLine()) != null) {
                return line;
            }
        }
        return null;
    }

    public String read(String prompt) throws IOException {
        System.out.printf("%s", prompt);
        return read();
    }
}
