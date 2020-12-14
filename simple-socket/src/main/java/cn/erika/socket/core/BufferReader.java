package cn.erika.socket.core;

import cn.erika.socket.common.component.DataInfo;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

public class BufferReader {
    private Charset charset;

    public BufferReader(Charset charset) {
        this.charset = charset;
    }

    public void read(TcpChannel channel, ByteBuffer buffer) {
        byte[] data = buffer.array();
        int len = data.length;
        if (len == 0 || len < DataInfo.LEN) {
            return;
        }
        byte[] bHead = new byte[DataInfo.LEN];
        System.arraycopy(data, 0, bHead, 0, DataInfo.LEN);
        String strHead = new String(bHead, charset);
        if("".equals(strHead.trim())){
            return;
        }
        DataInfo info = new DataInfo();
        // 时间戳 13字节
        info.setTimestamp(new Date(Long.parseLong(strHead.substring(0, 13))));
        info.setCompress(DataInfo.Compress.getByValue(Integer.parseInt(strHead.substring(13, 23))));
        // 本次传输偏移量 10字节
        info.setPos(Long.parseLong(strHead.substring(23, 33)));
        // 本次传输长度 10字节
        info.setLen(Integer.parseInt(strHead.substring(33, 43)));
        byte[] tmp = new byte[info.getLen()];
        System.arraycopy(data, DataInfo.LEN, tmp, 0, info.getLen());
        channel.receive(info, tmp);
    }
}
