package cn.erika.utils.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 从键盘读取字符的工具类
 */
public class KeyboardReader {
    // 获取本地字符集作为输入流的默认字符集 Linux=UTF-8 Win=GBK
    private static final String LOCAL_CODE = System.getProperty("file.encoding");
    // 单例模式 避免多实例冲突
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

    /**
     * 从键盘读取一行
     *
     * @return 读取到的文字
     * @throws IOException 如果输入流读取失败
     */
    public String read() throws IOException {
        if (reader != null) {
            String line;
            if ((line = reader.readLine()) != null) {
                return line;
            }
        }
        return null;
    }

    /**
     * 从键盘读取一行 带提示符
     *
     * @param prompt 提示符
     * @return 读取到的文字
     * @throws IOException 如果输入流读取失败
     */
    public String read(String prompt) throws IOException {
        System.out.printf("%s", prompt);
        return read();
    }
}
