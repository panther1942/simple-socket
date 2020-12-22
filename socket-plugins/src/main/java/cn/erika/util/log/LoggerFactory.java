package cn.erika.util.log;

import cn.erika.config.GlobalSettings;

public class LoggerFactory {
    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(GlobalSettings.logLevel, clazz);
    }

    public static Logger getLogger(int level, Class<?> clazz) {
        return getLogger(level, clazz.getName());
    }

    public static Logger getLogger(String name) {
        return getLogger(GlobalSettings.logLevel, name);
    }

    public static Logger getLogger(int level, String name) {
        return new ConsoleLogger(level, name);
    }
}
