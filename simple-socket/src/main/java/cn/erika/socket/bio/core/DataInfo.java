package cn.erika.socket.bio.core;

import java.io.Serializable;
import java.util.Date;

public class DataInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int LEN = 13 + 10 + 10 + 10;

    private Date timestamp;
    private Compress compress = Compress.NONE; // 0: none 1: gzip
    private long pos;
    private int len;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Compress getCompress() {
        return compress;
    }

    public void setCompress(Compress compress) {
        this.compress = compress;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    @Override
    public String toString() {
        String timestamp = String.format("%13s", this.timestamp.getTime()).replaceAll("\\s", "0");
        String compress = String.format("%10s", this.compress.value).replaceAll("\\s", "0");
        String pos = String.format("%10s", this.pos).replaceAll("\\s", "0");
        String len = String.format("%10s", this.len).replaceAll("\\s", "0");
        return timestamp + compress + pos + len;
    }

    public String toJson() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{\"timestamp\":\"").append(this.getTimestamp().getTime()).append("\",");
        buffer.append("\"compress\":\"").append(this.compress.value).append("\",");
        buffer.append("\"pos\":\"").append(this.pos).append("\",");
        buffer.append("\"len\":\"").append(this.len).append("\"}");
        return buffer.toString();
    }

    public enum Compress {
        NONE(0),
        GZIP(1);

        private int value;

        Compress(int value) {
            this.value = value;
        }

        public static Compress getByValue(int value) {
            for (Compress compress : Compress.values()) {
                if (compress.value == value) {
                    return compress;
                }
            }
            return null;
        }
    }
}
