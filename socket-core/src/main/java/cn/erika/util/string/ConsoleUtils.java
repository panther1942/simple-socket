package cn.erika.util.string;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleUtils {
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
    private static final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    private static SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);

    /**
     * 终端上画分割线
     *
     * @param character 分割线的字符
     * @param length    分割线的总长度
     */
    public static String drawLine(CharSequence character, int length) {
        return drawLineWithTitle(null, character, length);
    }

    public static String drawTitle(String title, int length) {
        return drawLineWithTitle(title, null, length);
    }

    /**
     * 终端上画分割线 带水平居中的标题
     * 当标题字数为奇数个时标题会偏左一个字符
     * 当标题为空时会设置为空字符串
     *
     * @param title     标题
     * @param character 分割线的字符
     * @param length    分割线的总长度（含标题）
     */
    public static String drawLineWithTitle(String title, CharSequence character, int length) {
        if (title == null) {
            title = "";
        }
        if (length < title.length()) {
            return title;
        }

        int i = 0;
        StringBuilder buffer = new StringBuilder();
        for (; i < (length - title.length()) / 2; i++) {
            buffer.append(character);
        }
        buffer.append(title);
        for (; i < length - title.length(); i++) {
            buffer.append(character);
        }
        return buffer.toString();
    }

    public static String consoleLog(String logLevel, Class originClass, String message) {
        // 处理时间日期
        Date now = new Date();
        Thread thread = Thread.currentThread();
        // 处理线程名称
        String threadName = thread.getName();
        if (threadName.length() > 15) {
            threadName = threadName.substring(0, 16);
        }
        // 处理类名
        String className = originClass.getName();
        if (className.length() > 40) {
            String[] target = checkLength(className.split("\\."), className.length(), 40, 0);
            StringBuffer stringBuffer = new StringBuffer();
            for (String str : target) {
                stringBuffer.append(str).append(".");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            className = stringBuffer.toString();
        }
        return String.format("%s %5s %5s --- [%15s] %-40s : %s",
                sdf.format(now),
                logLevel,
                pid,
                threadName,
                className,
                message);
    }

    private static String[] checkLength(String[] packagePath, int srcLength, int destLength, int pos) {
        int length = packagePath[pos].length();
        packagePath[pos] = packagePath[pos].substring(0, 1);
        srcLength = srcLength - length + 1;
        if (srcLength > destLength) {
            return checkLength(packagePath, srcLength, destLength, ++pos);
        }
        return packagePath;
    }
}
