package cn.erika.socket.core.tcp;

import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.pto.DataInfo;
import cn.erika.socket.exception.DataFormatException;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

// 根据自定协议实现的一个处理数据的类
class TcpReader {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Charset charset;

    private DataInfo info;
    private byte[] cache;
    private int pos = 0;

    TcpReader(Charset charset) {
        this.charset = charset;
    }

    // 因为一个socket或者channel对应一个reader 所以没必要加锁
    // 下面负责的逻辑是处理粘包的 因为尝试次数太多 忘了当初是咋想的了 并发数稍高一点就会出问题
    public synchronized void read(ISocket socket, byte[] data, int len) throws IOException, DataFormatException {
        // 因为数组长度和有效数据的长度很有可能不一致 因此需要按照读取的长度拷贝一次数组
        byte[] tmp = new byte[len];
        System.arraycopy(data, 0, tmp, 0, len);
        while (tmp != null && tmp.length > 0) {
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
                info.setData(cache);
                socket.receive(info);
                info = null;
                cache = null;
                pos = 0;
            }
        }
    }

    private byte[] getDataInfo(byte[] data, int len) throws DataFormatException {
        if (len == 0 || len < DataInfo.LEN) {
            return data;
        }
        byte[] bHead = new byte[DataInfo.LEN];
        System.arraycopy(data, 0, bHead, 0, DataInfo.LEN);
        String strHead = new String(bHead, charset);
        try {
            info = new DataInfo();
            // uuid 36字节
            info.setUuid(strHead.substring(0, 36));
            // 时间戳 13字节
            info.setTimestamp(new Date(Long.parseLong(strHead.substring(36, 49))));
            // 压缩 2字节
            info.setCompress(Integer.parseInt(strHead.substring(49, 51), 16));
            // 偏移量 10字节
            info.setPos(Long.parseLong(strHead.substring(51, 61)));
            // 长度 19字节
            info.setLen(Integer.parseInt(strHead.substring(61, 80)));
            byte[] tmp = new byte[len - DataInfo.LEN];
            System.arraycopy(data, DataInfo.LEN, tmp, 0, len - DataInfo.LEN);
            return tmp;
        } catch (NumberFormatException e) {
            throw new DataFormatException("意外的字符: " + e.getMessage());
        }
    }
}
