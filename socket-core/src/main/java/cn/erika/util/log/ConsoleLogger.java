package cn.erika.util.log;

/**
 * 输出日志到标准输出流
 */
public class ConsoleLogger implements LogPrinter {

    /**
     * 输出日志到控制台
     * 将Debug和Info信息输出到标准输出流
     * 将Warn和Error信息输出到标准错误流
     *
     * @param level 日志等级
     * @param line  输出的信息
     */
    @Override
    public synchronized void print(LogLevel level, String line) {
        if (level.getValue() >= LogLevel.WARN.getValue()) {
            System.err.println(line);
        } else {
            System.out.println(line);
        }
    }
}
