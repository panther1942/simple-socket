package cn.erika.utils.string;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 在控制台上打印信息的工具类
 */
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

    /**
     * 终端上画水平居中的标题(指定长度的中心)
     * 当标题字数为奇数个时标题会偏左一个字符
     * 当标题为空时会设置为空字符串
     *
     * @param title  分割线的字符
     * @param length 分割线的总长度
     */
    public static String drawTitle(String title, int length) {
        return drawLineWithTitle(title, null, length);
    }

    /**
     * 终端上画分割线 带水平居中的标题(指定长度的中心)
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

    /**
     * 模仿Spring的日志输出格式输出日志
     * 格式为: %s %5s %5s --- [%15s] %-40s : %s
     *
     * @param logLevel    日志等级
     * @param targetClass 目标类 用于获取相关信息
     * @param message     输出的日志消息
     * @return 格式化后的字符串
     */
    public static String consoleLog(String logLevel, Class<?> targetClass, String message) {
        // 处理时间日期
        Date now = new Date();
        Thread thread = Thread.currentThread();
        // 处理线程名称
        String threadName = thread.getName();
        if (threadName.length() > 15) {
            threadName = threadName.substring(0, 15);
        }
        // 处理类名
        String className = targetClass.getName();
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

    /**
     * 用于检查类的路径长度是否符合预期 如果不符合则从最外层的包名开始取首字母
     *
     * @param classPath  类路径
     * @param srcLength  原始长度
     * @param destLength 目标长度
     * @param pos        递归层数
     * @return 符合预期长度的字符串
     */
    private static String[] checkLength(String[] classPath, int srcLength, int destLength, int pos) {
        int length = classPath[pos].length();
        classPath[pos] = classPath[pos].substring(0, 1);
        srcLength = srcLength - length + 1;
        // 这块要注意 如果类路径的每个名字都取了首字母 但是长度依然不符合 就必须要退出递归 否则数组越界
        if (srcLength > destLength && pos < classPath.length) {
            return checkLength(classPath, srcLength, destLength, ++pos);
        }
        return classPath;
    }
}
