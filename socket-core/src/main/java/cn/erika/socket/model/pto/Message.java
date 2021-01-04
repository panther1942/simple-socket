package cn.erika.socket.model.pto;

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
        if (payload.containsKey(key)) {
            return (T) payload.get(key);
        } else {
            return null;
        }
    }

    public void del(String key) {
        payload.remove(key);
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "payload=" + payload +
                '}';
    }
}
