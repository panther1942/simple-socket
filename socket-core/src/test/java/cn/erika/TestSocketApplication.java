package cn.erika;

import cn.erika.config.GlobalSettings;
import cn.erika.context.annotation.PackageScan;
import cn.erika.socket.SocketApplication;
import cn.erika.utils.security.algorithm.BasicSecurityAlgorithm;

@PackageScan("cn.erika")
public class TestSocketApplication extends SocketApplication {

    public static void main(String[] args) {
        run(TestSocketApplication.class);
    }

    @Override
    protected void beforeStartup() {
        super.beforeStartup();
        GlobalSettings.securityAlgorithm = BasicSecurityAlgorithm.AES128GCM;
    }

    @Override
    protected void afterStartup() {
        super.afterStartup();
    }
}
