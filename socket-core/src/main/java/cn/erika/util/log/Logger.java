package cn.erika.util.log;

import cn.erika.util.string.ConsoleUtils;

import java.util.List;

public class Logger {
    private LogLevel level;
    private Class originClass;
    private List<LogPrinter> logPrinterList;

    public Logger(LogLevel level, Class originClass, List<LogPrinter> logPrinterList) {
        this.level = level;
        this.originClass = originClass;
        this.logPrinterList = logPrinterList;
    }

    private void prints(LogLevel level, Class originClass, String message) {
        if (this.level.getValue() <= level.getValue()) {
            String line = ConsoleUtils.consoleLog(level.getName(), originClass, message);
            for (LogPrinter printer : logPrinterList) {
                printer.print(level, line);
            }
        }
    }

    public void debug(String message) {
        prints(LogLevel.DEBUG, originClass, message);
    }

    public void debug(String message, Throwable throwable) {
        debug(message);
        throwable.printStackTrace();
    }

    public void info(String message) {
        prints(LogLevel.INFO, originClass, message);
    }

    public void info(String message, Throwable throwable) {
        info(message);
        throwable.printStackTrace();
    }

    public void warn(String message) {
        prints(LogLevel.WARN, originClass, message);
    }

    public void warn(String message, Throwable throwable) {
        warn(message);
        throwable.printStackTrace();
    }

    public void error(String message) {
        prints(LogLevel.ERROR, originClass, message);
    }

    public void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }
}
