package cn.erika.socket.component;

import cn.erika.config.Constant;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

// 消息的包装类
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Head {
        ServerName(),;
    }

    // 固定key值 方便服务解析
    private Map<Head, Object> head = new LinkedHashMap<>();
    // 自由key值 随便放数据
    private Map<String, Object> payload = new LinkedHashMap<>();
    // 签名确保数据完整性和有效性 签名取决于全局设定中的rsaAlgorithm
    // 目前仅支持<签名算法>withRSA 具体哪些可用因平台而异 至SHA-XwithRSA系列都能用
    private byte[] sign;

    public Message() {
    }

    // 服务名称是必须要填的 不然接收方不知道如何处理这些数据
    public Message(String serviceName) {
        this.head.put(Head.ServerName, serviceName);
    }

    // 如果第二个参数直接填字符串 则放在payload.message中 方便直接读取
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

    // 这里不要带上签名 不然会出麻烦
    @Override
    public String toString() {
        return "Message{" +
                "head=" + head +
                ", payload=" + payload +
                '}';
    }
}
