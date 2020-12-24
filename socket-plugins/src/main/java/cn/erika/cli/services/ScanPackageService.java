package cn.erika.cli.services;

import cn.erika.context.Application;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;

import java.io.IOException;

@Component("scan")
public class ScanPackageService extends BaseService implements CliService {
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
