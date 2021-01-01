package cn.erika;

import cn.erika.context.annotation.PackageScan;
import cn.erika.socket.SocketApplication;

@PackageScan("cn.erika")
public class TestSocketApplication extends SocketApplication {

    public static void main(String[] args) {
        run(TestSocketApplication.class);
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
