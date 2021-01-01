package cn.erika.utils.log;

import java.io.IOException;

/**
 * 日志的实现类要实现此接口
 */
public interface LogPrinter {

    /**
     * 输出日志
     *
     * @param level 日志等级
     * @param line  输出的信息
     * @throws IOException 如果日志输出出现错误则抛出该异常 例如无法写文件
     */
    void print(LogLevel level, String line) throws IOException;
}
