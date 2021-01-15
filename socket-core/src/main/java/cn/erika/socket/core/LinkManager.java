package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 客户端接入管理器
// 负责维护这些连接
// TODO 长时间空闲连接需要清理掉 无效连接也需要定时检测清理（心跳包ping什么的）
public class LinkManager {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Map<String, ISocket> linkList = new ConcurrentHashMap<>();
    private Timer timer = new Timer();
    private long cleanInterval = GlobalSettings.cleanInterval;
    private long invalidInterval = GlobalSettings.invalidInterval;

    public LinkManager() {
        log.info("启动定时清理器 清理间隔: " + cleanInterval / 1000 + "/" + invalidInterval / 1000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("开始检查连接");
                List<String> list = new LinkedList<>();
                Date now = new Date();
                for (String uuid : linkList.keySet()) {
                    ISocket socket = linkList.get(uuid);
                    if (socket.isClosed()) {
                        list.add(uuid);
                    } else {
                        Date lastTime = socket.get(Constant.LAST_TIME);
                        if (now.getTime() - lastTime.getTime() > invalidInterval) {
                            list.add(uuid);
                        }
                    }
                }
                for (String uuid : list) {
                    delLink(uuid);
                }
                log.info("清理连接数: " + list.size());
            }
        }, cleanInterval, cleanInterval);
    }

    public ISocket addLink(ISocket socket) {
        String uid = UUID.randomUUID().toString();
        socket.set(Constant.UID, uid);
        linkList.put(uid, socket);
        return socket;
    }

    public Map<String, ISocket> getLinks() {
        return this.linkList;
    }

    public ISocket getLink(String uid) {
        return linkList.get(uid);
    }

    public ISocket delLink(String uid) {
        ISocket socket = linkList.remove(uid);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        return socket;
    }

    public String isExistLink(ISocket socket) {
        for (String uid : linkList.keySet()) {
            if (uid.equals(socket.get(Constant.UID))) {
                return uid;
            }
        }
        return null;
    }

    public boolean popLink(ISocket socket) {
        String uid = isExistLink(socket);
        if (uid != null) {
            delLink(uid);
            return true;
        }
        return false;
    }

    public void stop() {
        timer.cancel();
    }
}
