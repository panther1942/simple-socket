package cn.erika.test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<Head, Object> head = new HashMap<>();
    private byte[] body;
    private byte[] sign;

    public void addHead(Head key, Object value) {
        this.head.put(key, value);
    }

    public <T> T getHead(Head key) {
        return (T) head.get(key);
    }

    public <T> T delHead(Head key) {
        return (T) head.remove(key);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }

    public enum Head {
        Encrypt("encrypt"),
        Sign("sign"),
        Order("order");

        private String value;

        Head(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}
