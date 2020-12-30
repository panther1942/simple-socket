package cn.erika.util.log;

import cn.erika.util.string.ConsoleUtils;

import java.io.IOException;
import java.util.List;

/**
 * 日志的控制类 定义了日志的不同级别的操作
 */
public class Logger {
    private LogLevel level;
    private Class targetClass;
    private List<LogPrinter> logPrinterList;

    /**
     * 由工厂类创建该实体对象
     *
     * @param level          指定日志的目标打印等级 低于该等级的日志将不会打印
     * @param targetClass    目标类 用于获取目标类的信息
     * @param logPrinterList 获取实际打印日志的对象 需要提前在工厂类中注册
     */
    Logger(LogLevel level, Class targetClass, List<LogPrinter> logPrinterList) {
        this.level = level;
        this.targetClass = targetClass;
        this.logPrinterList = logPrinterList;
    }

    private void prints(LogLevel level, String message) {
        // 如果工厂类中关闭了日志打印或者该目标类是被忽略的 则不打印日志
        if (!LoggerFactory.isEnable() || LoggerFactory.isIgnore(targetClass)) {
            return;
        }
        // 如果日志等级不低于设置的目标打印等级 则打印该日志
        if (this.level.getValue() <= level.getValue()) {
            // 日志的输出格式见ConsoleUtils.consoleLog方法
            String line = ConsoleUtils.consoleLog(level.getName(), targetClass, message);
            for (LogPrinter printer : logPrinterList) {
                try {
                    printer.print(level, line);
                } catch (IOException e) {
                    // 如果日志打印异常 则将错误信息输出到标准错误流中
                    System.err.println("打印日志异常: " + e.getMessage());
                }
            }
        }
    }

    public void debug(String message) {
        prints(LogLevel.DEBUG, message);
    }

    public void debug(String message, Throwable throwable) {
        debug(message);
        throwable.printStackTrace();
    }

    public void info(String message) {
        prints(LogLevel.INFO, message);
    }

    public void info(String message, Throwable throwable) {
        info(message);
        throwable.printStackTrace();
    }

    public void warn(String message) {
        prints(LogLevel.WARN, message);
    }

    public void warn(String message, Throwable throwable) {
        warn(message);
        throwable.printStackTrace();
    }

    public void error(String message) {
        prints(LogLevel.ERROR, message);
    }

    public void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }
}
