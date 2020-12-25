package cn.erika.socket.core.component;

import cn.erika.config.Constant;
import cn.erika.socket.core.ISocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 客户端接入管理器
// 负责维护这些连接
// TODO 长时间空闲连接需要清理掉 无效连接也需要定时检测清理（心跳包ping什么的）
public class LinkManager {
    private Map<String, ISocket> linkList = new HashMap<>();

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
        return linkList.remove(uid);
    }

    public String isExistLink(ISocket socket) {
        for (String uid : linkList.keySet()) {
            if (socket.equals(linkList.get(uid))) {
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
}
