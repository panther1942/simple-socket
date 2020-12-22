package cn.erika.util.log;

public abstract class Logger {

    protected int level;
    private String name;

    public Logger(int level, String name) {
        this.level = level;
        this.name = name;
    }

    protected abstract void print(int level, String prefix, String message);

    public void debug(String message) {
        print(LoggerFactory.DEBUG, name, message);
    }

    public void debug(String message, Throwable throwable) {
        debug(message);
        throwable.printStackTrace();
    }

    public void info(String message) {
        print(LoggerFactory.INFO, name, message);
    }

    public void info(String message, Throwable throwable) {
        info(message);
        throwable.printStackTrace();
    }

    public void warn(String message) {
        print(LoggerFactory.WARN, name, message);
    }

    public void warn(String message, Throwable throwable) {
        warn(message);
        throwable.printStackTrace();
    }

    public void error(String message) {
        print(LoggerFactory.ERROR, name, message);
    }

    public void error(String message, Throwable throwable) {
        error(message);
        throwable.printStackTrace();
    }
}
