package cn.erika.socket.core.component;

import cn.erika.socket.config.Constant;

import java.util.HashMap;
import java.util.Map;

public class Message {
    private Map<String, Object> payload = new HashMap<>();

    public Message(String message) {
        add(Constant.SERVICE_NAME, Constant.SRV_TEXT);
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
}
