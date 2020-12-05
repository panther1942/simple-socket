package cn.erika.socket.bio.core;

import cn.erika.socket.common.component.DataInfo;
import cn.erika.util.compress.CompressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

// 根据自定协议实现的一个处理数据的类
class Reader {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Charset charset;

    private DataInfo info;
    private byte[] cache;
    private int pos = 0;

    public Reader(Charset charset) {
        this.charset = charset;
    }

    synchronized void read(TcpSocket socket, byte[] data, int len) throws IOException, CompressException {
        byte[] tmp = new byte[len];
        System.arraycopy(data, 0, tmp, 0, len);
        while (tmp != null) {
            if (info == null) {
                byte[] tmp2 = new byte[pos + tmp.length];
                if (pos > 0) {
                    System.arraycopy(cache, 0, tmp2, 0, pos);
                }
                System.arraycopy(tmp, 0, tmp2, pos, tmp.length);
                tmp = getDataInfo(tmp2, tmp2.length);
                if (info != null) {
                    cache = new byte[info.getLen()];
                    pos = 0;
                } else {
                    break;
                }
            }
            int available = cache.length - pos;
            if (tmp.length <= available) {
                System.arraycopy(tmp, 0, cache, pos, tmp.length);
                pos += tmp.length;
                tmp = null;
            } else {
                System.arraycopy(tmp, 0, cache, pos, available);
                pos = cache.length;
                byte[] tmp2 = new byte[tmp.length - available];
                System.arraycopy(tmp, available, tmp2, 0, tmp.length - available);
                tmp = tmp2;
            }
            if (pos == cache.length && (pos > 0 || info.getLen() == 0)) {
                socket.receive(info, cache);
                info = null;
                cache = null;
                pos = 0;
            }
        }
    }

    private byte[] getDataInfo(byte[] data, int len) {
        if (len < DataInfo.LEN) {
            return data;
        }
        byte[] bHead = new byte[DataInfo.LEN];
        System.arraycopy(data, 0, bHead, 0, DataInfo.LEN);
        String strHead = new String(bHead, charset);
        info = new DataInfo();
        // 时间戳 13字节
        info.setTimestamp(new Date(Long.parseLong(strHead.substring(0, 13))));
        info.setCompress(DataInfo.Compress.getByValue(Integer.parseInt(strHead.substring(13, 23))));
        // 本次传输偏移量 10字节
        info.setPos(Long.parseLong(strHead.substring(23, 33)));
        // 本次传输长度 10字节
        info.setLen(Integer.parseInt(strHead.substring(33, 43)));
        byte[] tmp = new byte[len - DataInfo.LEN];
        System.arraycopy(data, DataInfo.LEN, tmp, 0, len - DataInfo.LEN);
//        log.debug(info.toJson());
//        log.debug(info.toString());
        return tmp;
    }
}
