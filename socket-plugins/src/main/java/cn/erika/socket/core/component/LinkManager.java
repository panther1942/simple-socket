package cn.erika.socket.core.component;

import cn.erika.config.Constant;
import cn.erika.socket.core.Socket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 客户端接入管理器
// 负责维护这些连接
// TODO 长时间空闲连接需要清理掉 无效连接也需要定时检测清理（心跳包ping什么的）
public class LinkManager {
    private Map<String, Socket> linkList = new HashMap<>();

    public Socket addLink(Socket socket) {
        String uid = UUID.randomUUID().toString();
        socket.set(Constant.UID, uid);
        linkList.put(uid, socket);
        return socket;
    }

    public Map<String, Socket> getLinks() {
        return this.linkList;
    }

    public Socket getLink(String uid) {
        return linkList.get(uid);
    }

    public Socket delLink(String uid) {
        return linkList.remove(uid);
    }

    public String isExistLink(Socket socket) {
        for (String uid : linkList.keySet()) {
            if (socket.equals(linkList.get(uid))) {
                return uid;
            }
        }
        return null;
    }

    public boolean popLink(Socket socket) {
        String uid = isExistLink(socket);
        if (uid != null) {
            delLink(uid);
            return true;
        }
        return false;
    }
}
