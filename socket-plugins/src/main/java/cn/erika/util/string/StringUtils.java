package cn.erika.util.string;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static Random random = new Random();

    public static String randomString(int len) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int idx = random.nextInt(65) + 58;
            buffer.append(String.valueOf((char) idx));
        }
        return buffer.toString();
    }

    public static byte[] randomByte(int length) {
        return randomString(length).getBytes(Charset.forName("UTF-8"));
    }

    public static String[] getParam(String line) {
        List<String> list = new ArrayList<>();
        String regex = "(\"[[^\"].]+\"|\'[[^\'].]+\'|[\\S]+)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);
        while (m.find()) {
            list.add(m.group(1).replaceAll("\"", "").replaceAll("\'", ""));
        }
        return list.toArray(new String[list.size()]);
    }

    public static boolean isEmpty(String target) {
        return target == null || "".equals(target.trim());
    }

    public static String byteToHexString(byte[] data) {
        StringBuilder buffer = new StringBuilder();
        for (byte b : data) {
            int i = b;
            if (i < 0)
                i += 256;
            if (i < 16)
                buffer.append(0);
            buffer.append(Integer.toHexString(i));
        }
        return buffer.toString();
    }
}
