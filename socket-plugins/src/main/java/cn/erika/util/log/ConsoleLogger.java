package cn.erika.util.log;

public class ConsoleLogger extends Logger {

    ConsoleLogger(int level, String name) {
        super(level, name);
    }

    @Override
    protected void print(int level, String prefix, String message) {
        if (level >= this.level) {
            System.out.println(prefix + " " + message);
        }
    }
}
