package cn.erika.socket.core.component;

import cn.erika.config.Constant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Object> payload = new HashMap<>();

    public Message() {
    }

    public Message(String serviceName) {
        add(Constant.SERVICE_NAME, serviceName);
    }

    public Message(String serviceName, String message) {
        add(Constant.SERVICE_NAME, serviceName);
        add(Constant.TEXT, message);
    }

    public void add(String key, Object value) {
        payload.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) payload.get(key);
    }

    public void del(String key) {
        payload.remove(key);
    }

    @Override
    public String toString() {
        return "Message{" +
                "payload=" + payload +
                '}';
    }
}
