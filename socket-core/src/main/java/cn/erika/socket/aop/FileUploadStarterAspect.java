package cn.erika.socket.aop;

import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.bean.Advise;
import cn.erika.socket.core.Handler;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.handler.bio.FileSender;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

import java.lang.reflect.Method;

@Component
public class FileUploadStarterAspect implements Advise {
    private Logger log = LoggerFactory.getLogger(this.getClass());
//    upload /home/erika/Downloads/config.json config.json

    @Override
    public void before(Method method, Object[] args) {
    }

    @Override
    public void afterReturning(Method method, Object[] args, Object result) {
        if (args.length > 1 && args[1] != null) {
            ISocket socket = (ISocket) args[0];
            Message message = (Message) args[1];
            log.info("准备发送文件: " + message.get(Constant.FILENAME));
            Handler handler = socket.getHandler();
            if (FileSender.class.isInstance(handler)) {
                FileSender fileSender = (FileSender) handler;
                fileSender.upload();
            }
        }
    }

    @Override
    public void afterThrowing(Method method, Object[] args, Throwable error) {
    }
}
