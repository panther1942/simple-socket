package cn.erika.cli.services;

import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.util.log.LogLevel;

@Component("log")
public class DebugService extends BaseService implements ICliService {

    @Override
    public String info() {
        return "启用/禁用日志和设置日志等级\n" +
                "\t如果不加参数将切换启用或者禁用日志\n" +
                "\t日志等级包括DEBUG INFO WARN ERROR";
    }

    @Override
    public void execute(String... args) throws BeanException {
        try {
            LogLevel level = LogLevel.getByName(args[1]);
            if (level != null) {
                GlobalSettings.logLevel = level;
                log.info("切换日志等级为: " + level.getName());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            GlobalSettings.logEnable = !GlobalSettings.logEnable;
            System.out.println(String.format("%s日志",
                    (GlobalSettings.logEnable ? "启用" : "禁用")));
        }
    }
}
