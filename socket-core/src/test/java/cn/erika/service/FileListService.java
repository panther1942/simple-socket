package cn.erika.service;

import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ISocketService;

import java.io.File;

@Component(Constant.SRV_FILE_LIST)
public class FileListService extends BaseService implements ISocketService {

    @Override
    public void client(ISocket socket, Message message) {
        if (message != null) {
            if (message.get(Constant.SERVICE_NAME) == null) {
                String dir = message.get(Constant.FILEPATH);
                Message request = new Message(Constant.SRV_FILE_LIST);
                request.add(Constant.FILEPATH, dir);
                socket.send(request);
            } else {
                Boolean result = message.get(Constant.RESULT);
                String line = message.get(Constant.TEXT);
                if (result != null && result) {
                    String[] filenameList = line.split(":");
                    for (String filename : filenameList) {
                        System.out.println(filename);
                    }
                } else {
                    System.out.println(line);
                }
            }
        }
    }

    @Override
    public void server(ISocket socket, Message message) {
        String dir = message.get(Constant.FILEPATH);
        if (dir == null) {
            dir = System.getProperty("user.dir");
        }
        File target = new File(dir);
        Message reply = new Message(Constant.SRV_FILE_LIST);
        if (!target.exists()) {
            reply.add(Constant.RESULT, false);
            reply.add(Constant.TEXT, "目录不存在");
        } else if (target.isDirectory()) {
            File[] files = target.listFiles();
            StringBuffer buffer = new StringBuffer();
            if (files.length != 0) {
                for (File file : files) {
                    buffer.append(file.getAbsolutePath() + ":");
                }
                buffer.deleteCharAt(buffer.length() - 1);
            } else {
                buffer.append("空目录");
            }
            reply.add(Constant.RESULT, true);
            reply.add(Constant.TEXT, buffer.toString());
        } else {
            reply.add(Constant.RESULT, true);
            reply.add(Constant.TEXT, target.getAbsolutePath());
        }
        socket.send(reply);
    }
}
