package cn.erika.util.log;

public abstract class Logger {

    protected LogLevel level;
    private Class originClass;

    public Logger(LogLevel level, Class originClass) {
        this.level = level;
        this.originClass = originClass;
    }

    protected abstract void print(LogLevel level, Class originClass, String message);

    public void debug(String message) {
        print(LogLevel.DEBUG, originClass, message);
    }

    public void debug(String message, Throwable throwable) {
        debug(message);
        throwable.printStackTrace();
    }

    public void info(String message) {
        print(LogLevel.INFO, originClass, message);
    }

    public void info(String message, Throwable throwable) {
        info(message);
        throwable.printStackTrace();
    }

    public void warn(String message) {
        print(LogLevel.WARN, originClass, message);
    }

    public void warn(String message, Throwable throwable) {
        warn(message);
        throwable.printStackTrace();
    }

    public void error(String message) {
        print(LogLevel.ERROR, originClass, message);
    }

    public void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }
}
