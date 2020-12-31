package cn.erika;

import cn.erika.cli.CliApplication;
import cn.erika.config.GlobalSettings;
import cn.erika.context.annotation.PackageScan;
import cn.erika.util.SecurityAlgorithmExtra;

@PackageScan("cn.erika")
public class TestCliApplication extends CliApplication {

    public static void main(String[] args) {
        run(TestCliApplication.class);
    }

    @Override
    protected void beforeStartup() {
        super.beforeStartup();
        GlobalSettings.securityAlgorithm = SecurityAlgorithmExtra.AES128CFB;
    }

    @Override
    protected void afterStartup() {
        super.afterStartup();
    }
}
