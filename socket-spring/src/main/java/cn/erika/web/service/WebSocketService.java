package cn.erika.web.service;

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

    private static ConcurrentHashMap<String, WebSocketService> clientList = new ConcurrentHashMap<>();
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) throws IOException {
        this.session = session;
        clientList.put(this.session.getId(), this);
    }

    @OnClose
    public void onClose() {
        if (this.session != null) {
            clientList.remove(this.session.getId());
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            System.out.println(message);
            sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.err.println("发生错误:");
        System.err.println("错误信息: " + t.getMessage());
        t.printStackTrace();
    }

    public static void sendMessageToAll(String message) throws IOException {
        for (String client : clientList.keySet()) {
            clientList.get(client).sendMessage(message);
        }
    }

    public static void sendMessage(String userId, String message) throws IOException {
        clientList.get(userId).sendMessage(message);
    }

    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
}
