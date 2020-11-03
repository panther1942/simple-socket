package cn.erika.test.socket.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Charset CHARSET = Charset.forName("UTF-8");

    public enum Head {
        REQUEST("request"),;

        private String value;

        Head(String value) {
            this.value = value;
        }
    }

    private Map<Head, Object> head = new HashMap<>();
    private byte[] payload;
    private byte[] sign;

    public Message() {
    }

    public Message(String serviceName, byte[] data) {
        this.head.put(Head.REQUEST, serviceName);
        this.payload = data;
    }

    public Message(String serviceName, String data) {
        this.head.put(Head.REQUEST, serviceName);
        this.payload = data.getBytes(CHARSET);
    }

    public Message(String serviceName, Object data) {
        this.head.put(Head.REQUEST, serviceName);
        this.payload = JSON.toJSONBytes(data);
    }

    public Message(String serviceName, Map<String, Object> data) {
        this.head.put(Head.REQUEST, serviceName);
        this.payload = new JSONObject(data).toJSONString().getBytes(CHARSET);
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

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
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
                ", payload=" + Arrays.toString(payload) +
                '}';
    }
}
