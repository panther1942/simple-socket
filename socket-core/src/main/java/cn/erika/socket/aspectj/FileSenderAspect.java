package cn.erika.socket.aspectj;

import cn.erika.config.Constant;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.FileInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.handler.bio.FileSender;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Date;

@Aspect
public class FileSenderAspect {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* cn.erika.socket.handler.bio.FileSender.onMessage(..))")
    public void onMessage() {
    }

    @Pointcut("execution(* cn.erika.socket.services.impl.fileTransfer.FileUploadService.client(..))")
    public void sendFile() {
    }

    @AfterReturning(value = "onMessage() && args(socket,message)", argNames = "joinPoint,socket,message")
    public void afterReturning(JoinPoint joinPoint, ISocket socket, Message message) {
        if (Constant.SRV_EXCHANGE_TOKEN.equalsIgnoreCase(message.get(Constant.SERVICE_NAME))) {
            boolean result = message.get(Constant.RESULT);
            if (result) {
                FileSender fileSender = (FileSender) joinPoint.getTarget();
                fileSender.upload();
            }
        }
    }

//    upload /home/erika/Downloads/config.json config.json
//    upload /home/erika/Downloads/aspectj-1.9.6.jar aspectj.jar
//    upload /home/erika/Downloads/81053568_0.jpg 81053568_0.jpg
//    upload /home/erika/Downloads/apipost_3.2.3-linux-x64.tar.xz apipost.tar.xz

    //    upload /home/erika/Downloads/phpMyAdmin-5.0.4-all-languages.zip phpMyAdmin.zip
    //    upload /home/erika/Downloads/linux_history.png linux_history.png
    //    upload /home/erika/Downloads/install-release.sh install-release.sh

    @Around(value = "sendFile() && args(socket,message)", argNames = "joinPoint,socket,message")
    public Object beforeSendFile(ProceedingJoinPoint joinPoint, ISocket socket, Message message) throws Throwable {

        Object[] params = new Object[2];
        params[0] = socket;
        params[1] = message;

        FileInfo info = message.get(Constant.FILE_INFO);
        long len = info.getFileLength();
        Date start = new Date();
        Object result = joinPoint.proceed(params);
        Date end = new Date();

        double time = ((double) end.getTime() - start.getTime()) / 1000;
        double speed = len / time / 1024;
        log.info(String.format("传输用时: %f 秒, 平均速率: %f kb/s", time, speed));
        return result;
    }

}
