package cn.erika.util.log;

import cn.erika.config.GlobalSettings;

import java.util.LinkedList;
import java.util.List;

public class LoggerFactory {
    private static List<LogPrinter> logPrinterList = new LinkedList<>();
    private static List<Class> ignoreList = new LinkedList<>();

    public static void register(LogPrinter printer) {
        logPrinterList.add(printer);
    }

    public static void unregister(LogPrinter printer){
        logPrinterList.remove(printer);
    }

    public static void ignore(Class clazz) {
        ignoreList.add(clazz);
    }

    public static boolean isIgnore(Class clazz) {
        return ignoreList.contains(clazz);
    }

    public static Logger getLogger(Class originClass) {
        return getLogger(GlobalSettings.logLevel, originClass);
    }

    public static Logger getLogger(LogLevel level, Class originClass) {
        return new Logger(level, originClass, logPrinterList);
    }
}
