package cn.erika.socket.services.impl.remote.fileManager;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.FileAttributes;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.string.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Component(Constant.SRV_LIST)
public class List extends BaseService implements ISocketService {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void client(ISocket socket, Message message) {
        if (message != null) {
            if (message.get(Constant.SERVICE_NAME) == null) {
                String dir = message.get(Constant.FILEPATH);
                Message request = new Message(Constant.SRV_LIST);
                request.add(Constant.FILEPATH, dir);
                socket.send(request);
            } else {
                Boolean result = message.get(Constant.RESULT);
                String line = message.get(Constant.TEXT);
                Map<String, FileAttributes> fileAttrsMap = message.get(Constant.FILE_ATTR);

                if (result != null && result) {
                    if (line != null) {
                        System.out.println(line);
                    }
                    if (fileAttrsMap != null) {
                        LinkedList<String> filenames = new LinkedList<>();
                        filenames.addAll(fileAttrsMap.keySet());
                        filenames.sort(new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o1.compareToIgnoreCase(o2);
                            }
                        });
                        for (String filename : filenames) {
                            FileAttributes attr = fileAttrsMap.get(filename);
                            System.out.printf("%9s %8s %8s %12d %19s %s\n",
                                    attr.getPosix(),
                                    attr.getOwner(),
                                    attr.getGroup(),
                                    attr.getLength(),
                                    sdf.format(attr.getLastModifiedTime()),
                                    attr.getFilename());
                        }
                    }
                } else {
                    System.out.println(line);
                }
            }
        }
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void server(ISocket socket, Message message) {
        String dir = message.get(Constant.FILEPATH);
        String pwd = socket.get(Constant.PWD);
        File target = null;
        if (!StringUtils.isEmpty(dir)) {
            target = new File(dir);
        } else if (!StringUtils.isEmpty(pwd)) {
            target = new File(pwd);
        } else {
            target = new File(System.getProperty("user.dir"));
        }

        Message reply = new Message(Constant.SRV_LIST);
        if (!target.exists()) {
            reply.add(Constant.RESULT, false);
            reply.add(Constant.TEXT, "目录不存在");
        } else if (target.isDirectory()) {
            File[] files = target.listFiles();
            Map<String, FileAttributes> fileAttributesMap = new HashMap<>();

            if (files != null && files.length != 0) {
                for (File file : files) {
                    try {
                        Path path = Paths.get(file.getAbsolutePath());
                        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
                        Object owner = Files.getAttribute(path, "unix:owner");
                        Object group = Files.getAttribute(path, "unix:group");
                        FileTime lastModifiedTime = Files.getLastModifiedTime(path);

                        FileAttributes attr = new FileAttributes();
                        attr.setFilename(file.getAbsolutePath());
                        attr.setPosix(PosixFilePermissions.toString(permissions));
                        attr.setOwner(owner.toString());
                        attr.setGroup(group.toString());
                        attr.setLastModifiedTime(new Date(lastModifiedTime.toMillis()));
                        attr.setLength(file.length());

                        fileAttributesMap.put(file.getName(), attr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                reply.add(Constant.RESULT, true);
                reply.add(Constant.FILE_ATTR, fileAttributesMap);
            } else {

                reply.add(Constant.RESULT, false);
                reply.add(Constant.TEXT, "目录为空或者不可读");
            }
        } else {
            reply.add(Constant.RESULT, true);
            reply.add(Constant.TEXT, target.getAbsolutePath());
        }
        socket.send(reply);
    }
}
