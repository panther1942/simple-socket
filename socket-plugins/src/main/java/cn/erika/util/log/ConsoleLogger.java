package cn.erika.util.log;

import cn.erika.util.string.ConsoleUtils;

public class ConsoleLogger extends Logger {

    ConsoleLogger(LogLevel level, Class originClass) {
        super(level, originClass);
    }

    @Override
    protected synchronized void print(LogLevel level, Class originClass, String message) {
        if (level.getValue() >= this.level.getValue()) {
            String line = ConsoleUtils.consoleLog(level.getName(), originClass, message);
            if (level.getValue() >= LogLevel.WARN.getValue()) {
                System.err.println(line);
            } else {
                System.out.println(line);
            }
        }
    }
}
