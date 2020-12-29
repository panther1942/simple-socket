package cn.erika.cli.services;

import cn.erika.context.Application;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;

import java.io.IOException;

@Component("scan")
public class ScanPackageService extends BaseService implements ICliService {
    @Override
    public String info() {
        return "[!失败!] 用于重新扫描类路径下新增的类文件\n" +
                "\t目前只能重新加载新增的类 对于修改的类无法重新装载";
    }

    @Override
    public void execute(String... args) throws BeanException {
        try {
            log.debug("重新扫描");
            Application.scanPackage();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
