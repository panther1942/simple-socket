package cn.erika.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{clientId}")
@Component
public class WebSocketService implements Serializable {
    public static final long serialVersionUID = 1L;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static ConcurrentHashMap<String, Session> clientList = new ConcurrentHashMap<>();

    private String clientId;
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) throws IOException {
        this.clientId = clientId;
        this.session = session;
        clientList.put(clientId, session);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println(message);
        if ("server".equalsIgnoreCase(clientId)) {
            sendMessage(message);
        } else {
            Session server = clientList.get("server");
            if (server != null) {
                sendMessage(server, message);
            } else {
                sendMessage(session, "服务器不在线");
            }
        }
    }

    @OnClose
    public void onClose() {
        clientList.remove(clientId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        log.debug("WebSocket错误: " + throwable.getMessage(), throwable);
        if (session.isOpen()) {
            session.close();
        }
        onClose();
    }

    private static void sendMessage(Session session, String message) {
        session.getAsyncRemote().sendText(message);
    }

    public static void sendMessage(String clientId, String message) {
        sendMessage(clientList.get(clientId), message);
    }

    public static void sendMessage(String message) {
        for (String clientId : clientList.keySet()) {
            sendMessage(clientId, message);
        }
    }
}
