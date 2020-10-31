package cn.erika.socket.core;

import java.io.Serializable;
import java.util.Date;

class DataInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int LEN = 13 + 10 + 10;

    private Date timestamp;
    private int len;
    private long pos;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        String timestamp = String.format("%13s", this.timestamp.getTime()).replaceAll(" ", "0");
        String pos = String.format("%10s", this.pos).replaceAll("\\s", "0");
        String len = String.format("%10s", this.len).replaceAll("\\s", "0");
        return timestamp + pos + len;
    }
}
