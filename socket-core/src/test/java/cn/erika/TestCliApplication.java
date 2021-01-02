package cn.erika;

import cn.erika.cli.CliApplication;
import cn.erika.context.annotation.PackageScan;

@PackageScan("cn.erika")
public class TestCliApplication extends CliApplication {

    public static void main(String[] args) {
        run(TestCliApplication.class);
    }

    @Override
    protected void beforeStartup() {
        super.beforeStartup();
    }

    @Override
    protected void afterStartup() {
        super.afterStartup();
    }
}
