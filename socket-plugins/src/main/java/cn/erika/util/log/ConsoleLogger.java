package cn.erika.util.log;

import cn.erika.config.GlobalSettings;

public class ConsoleLogger implements LogPrinter {

    @Override
    public synchronized void print(LogLevel level, String line) {
        if (!GlobalSettings.logEnable) {
            return;
        }
        if (level.getValue() >= LogLevel.WARN.getValue()) {
            System.err.println(line);
        } else {
            System.out.println(line);
        }
    }
}
