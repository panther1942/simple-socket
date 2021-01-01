package cn.erika.utils.log;

/**
 * 规定日志的等级
 * 目前就四个级别 debug info warn error
 * 至于咋用看你心情
 * 个人认为四个等级足够了
 * Debug用于调试 生产环境至少用Info
 * Info打印标准输出
 * Warn打印警告信息 就是不影响程序运行 但是不符合预期运行逻辑
 * Error打印错误信息 就是程序运行出错了
 */
public enum LogLevel {
    DEBUG("DEBUG", 0),
    INFO("INFO", 1),
    WARN("WARN", 2),
    ERROR("ERROR", 3),;

    private String name;
    private int value;

    LogLevel(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public static LogLevel getByValue(int value) {
        for (LogLevel level : LogLevel.values()) {
            if (level.value == value) {
                return level;
            }
        }
        return null;
    }

    public static LogLevel getByName(String name) {
        for (LogLevel level : LogLevel.values()) {
            if (level.getName().equalsIgnoreCase(name)) {
                return level;
            }
        }
        return null;
    }
}
