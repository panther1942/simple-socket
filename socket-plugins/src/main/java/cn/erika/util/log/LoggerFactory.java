package cn.erika.util.log;

import cn.erika.config.GlobalSettings;

public class LoggerFactory {
    public static Logger getLogger(Class<?> originClass) {
        return getLogger(GlobalSettings.logLevel, originClass);
    }

    public static Logger getLogger(LogLevel level, Class<?> originClass) {
        return new ConsoleLogger(level, originClass);
    }
}
