package cn.erika.socket.component;

import cn.erika.config.Constant;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    public enum Head {
        Order("title"),;

        private String value;

        Head(String value) {
            this.value = value;
        }
    }

    private Map<Head, Object> head = new LinkedHashMap<>();
    private Map<String, Object> payload = new LinkedHashMap<>();
    private byte[] sign;

    public Message() {
    }

    public Message(String serviceName) {
        this.head.put(Head.Order, serviceName);
    }

    public Message(String serviceName, String message) {
        this(serviceName);
        this.payload.put(Constant.MESSAGE, message);
    }

    public void add(String key, Object value) {
        this.payload.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) payload.get(key);
    }

    public void set(Map<String, Object> payload) {
        this.payload = payload;
    }

    public void addHead(Head key, Object value) {
        this.head.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getHead(Head key) {
        return (T) head.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T delHead(Head key) {
        return (T) head.remove(key);
    }

    public Map<Head, Object> getHead() {
        return this.head;
    }

    public void setHead(Map<Head, Object> head) {
        this.head = head;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "Message{" +
                "head=" + head +
                ", payload=" + payload +
                '}';
    }
}
