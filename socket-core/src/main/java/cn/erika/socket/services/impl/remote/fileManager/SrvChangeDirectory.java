package cn.erika.socket.services.impl.remote.fileManager;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.string.StringUtils;

import java.io.File;

@Component(Constant.SRV_CD)
public class SrvChangeDirectory extends BaseService implements ISocketService {
    @Override
    public void client(ISocket socket, Message message) {
        if (message != null) {
            if (message.get(Constant.SERVICE_NAME) == null) {
                String remoteDir = message.get(Constant.REMOTE_FILE);
                Message request = new Message(Constant.SRV_CD);
                request.add(Constant.REMOTE_FILE, remoteDir);
                socket.send(request);
            } else {
                Boolean result = message.get(Constant.RESULT);
                if (result != null && result) {
                    System.out.println("切换到目录: " + message.get(Constant.TEXT));
                } else {
                    System.err.println((String) message.get(Constant.TEXT));
                }
            }
        }
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void server(ISocket socket, Message message) {
        String remoteDir = message.get(Constant.REMOTE_FILE);
        String pwd = socket.get(Constant.PWD);
        if (StringUtils.isEmpty(remoteDir)) {
            remoteDir = pwd;
        } else if (!remoteDir.startsWith("/")) {
            remoteDir = pwd + "/" + remoteDir;
        }
        String[] dirs = remoteDir.split("/");
        int count = 0;
        String[] targetDirs = new String[dirs.length];
        for (int i = 0; i < dirs.length; i++, count++) {
            if (".".equals(dirs[i])) {
                count--;
            } else if ("..".equals(dirs[i])) {
                count -= 2;
            } else {
                targetDirs[count] = dirs[i];
            }
        }
        StringBuffer buffer = new StringBuffer("/");
        for (int i = 0; i < count; i++) {
            buffer.append(targetDirs[i]).append("/");
        }
        remoteDir = buffer.toString();

        Message reply = new Message(Constant.SRV_CD);
        File file = new File(remoteDir);
        if (!file.exists()) {
            reply.add(Constant.RESULT, false);
            reply.add(Constant.TEXT, "目标路径不存在: " + remoteDir);
        } else if (!file.isDirectory()) {
            reply.add(Constant.RESULT, false);
            reply.add(Constant.TEXT, "目标路径不是目录: " + file.getAbsolutePath());
        } else if (!file.canExecute()) {
            reply.add(Constant.RESULT, false);
            reply.add(Constant.TEXT, "没有进入该目录的权限: " + file.getAbsolutePath());
        } else {
            reply.add(Constant.RESULT, true);
            reply.add(Constant.TEXT, file.getAbsolutePath());
            socket.set(Constant.PWD, file.getAbsolutePath());
        }
        socket.send(reply);
    }
}
