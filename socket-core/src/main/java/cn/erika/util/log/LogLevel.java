package cn.erika.util.log;

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
