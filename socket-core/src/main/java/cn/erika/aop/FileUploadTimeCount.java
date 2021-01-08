package cn.erika.aop;

import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.bean.Advise;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FileUploadTimeCount implements Advise {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<FileInfo, Date> fileInfoMap = new ConcurrentHashMap<>();

    @Override
    public void before(Method method, Object[] args) {
        Message message = (Message) args[1];
        FileInfo fileInfo = message.get(Constant.FILE_INFO);
        fileInfoMap.put(fileInfo, new Date());
    }

    @Override
    public void afterReturning(Method method, Object[] args, Object result) {
        Message message = (Message) args[1];
        FileInfo fileInfo = message.get(Constant.FILE_INFO);
        Date start = fileInfoMap.get(fileInfo);
        Date end = new Date();
        long len = fileInfo.getLength();

        double time = ((double) end.getTime() - start.getTime()) / 1000;
        double speed = len / time / 1024;
        log.info(String.format("传输用时: %f 秒, 平均速率: %f kb/s", time, speed));
    }

    @Override
    public void afterThrowing(Method method, Object[] args, Throwable error) {

    }
}
