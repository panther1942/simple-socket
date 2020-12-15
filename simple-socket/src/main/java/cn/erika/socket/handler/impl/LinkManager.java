package cn.erika.socket.handler.impl;

import cn.erika.config.Constant;
import cn.erika.socket.core.BaseSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LinkManager {
    private Map<String, BaseSocket> linkList = new HashMap<>();

    BaseSocket addLink(BaseSocket socket) {
        String uid = UUID.randomUUID().toString();
        socket.set(Constant.UID, uid);
        linkList.put(uid, socket);
        return socket;
    }

    Map<String, BaseSocket> getLinks() {
        return this.linkList;
    }

    BaseSocket getLink(String uid) {
        return linkList.get(uid);
    }

    BaseSocket delLink(String uid) {
        return linkList.remove(uid);
    }

    String isExistLink(BaseSocket socket) {
        for (String uid : linkList.keySet()) {
            if (socket.equals(linkList.get(uid))) {
                return uid;
            }
        }
        return null;
    }

    boolean popLink(BaseSocket socket) {
        String uid = isExistLink(socket);
        if (uid != null) {
            delLink(uid);
            return true;
        }
        return false;
    }
}
