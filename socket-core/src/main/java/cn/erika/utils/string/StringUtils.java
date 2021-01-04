package cn.erika.utils.string;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static Random random = new Random();
    private static byte[] hexMap = new byte[]{
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * 生成指定长度的随机字符串
     * 生成的范围为ascii表中的33-126字符 均为可见字符
     *
     * @param len 指定生成的长度
     * @return 生成的字符串
     */
    public static String randomString(int len) {
        return randomString(len, 33, 126);
    }

    public static String randomNumber(int len) {
        return randomString(len, 48, 57);
    }

    public static String randomString(int len, int start, int end) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int idx = random.nextInt(end - start + 1) + start;
            buffer.append(String.valueOf((char) idx));
        }
        return buffer.toString();
    }

    /**
     * 从一个字符串中拆分出由空格分割的各个部分
     * 不拆分引号括住的部分
     *
     * @param line 一行字符串
     * @return 空格分隔的各个部分
     */
    public static String[] getParam(String line) {
        List<String> list = new ArrayList<>();
        String regex = "(`[[^`].]+`|\"[[^\"].]+\"|\'[[^\'].]+\'|[\\S]+)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);
        while (m.find()) {
            list.add(m.group(1)
                    .replaceAll("\"", "")
                    .replaceAll("\'", "")
                    .replaceAll("`", ""));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 判断字符串是否为空
     * 符合以下条件将判断为空
     * 1、本身为空
     * 2、调用trim后为空串
     *
     * @param target 要检查的字符串
     * @return 判断结果
     */
    public static boolean isEmpty(String target) {
        return target == null || "".equals(target.trim());
    }

    /**
     * 将字节数组转为16进制字符串
     *
     * @param data 字节数组
     * @return 转化为16进制的字符串
     */
    public static String byte2HexString(byte[] data) {
        StringBuilder buffer = new StringBuilder();
        for (byte b : data) {
            buffer.append(String.format("%02X", b & 0xFF));
        }
        return buffer.toString();
    }

    /**
     * 将16进制字符串转为字节数组
     *
     * @param hexString 16进制的字符串
     * @return 转化的字节数组
     */
    public static byte[] hexString2Byte(String hexString) {
        hexString = hexString.toUpperCase();
        byte[] arr = new byte[hexString.length() / 2];
        char[] hexChars = hexString.toCharArray();

        for (int i = 0; i < arr.length; i++) {
            int pos = i * 2;
            arr[i] = (byte) (getHexByte(hexChars[pos]) << 4 |
                    getHexByte(hexChars[pos + 1]));
        }
        return arr;
    }

    private static byte getHexByte(char c) {
        for (int i = 0; i < hexMap.length; i++) {
            if (hexMap[i] == c) {
                return (byte) i;
            }
        }
        return '\0';
    }

    public static StringBuffer join(CharSequence delimiter, Object[] array) {
        StringBuffer buffer = new StringBuffer();
        for (Object obj : array) {
            buffer.append(String.valueOf(obj)).append(delimiter);
        }
        buffer.deleteCharAt(buffer.length() - 1);
        return buffer;
    }
}
