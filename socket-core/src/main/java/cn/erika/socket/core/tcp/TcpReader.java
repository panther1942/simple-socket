package cn.erika.socket.core.tcp;

import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.Reader;
import cn.erika.socket.exception.DataFormatException;
import cn.erika.socket.model.pto.DataInfo;

// 根据自定协议实现的一个处理数据的类
class TcpReader implements Reader {
    private DataInfo dataInfo;
    private byte[] cache = new byte[0];
    private int pos = 0;
    private byte[] temp = null;


    // 因为一个socket或者channel对应一个reader 所以没必要加锁
    // 下面负责的逻辑是处理粘包的
    public void read(ISocket socket, byte[] data, int len) throws DataFormatException {
        byte[] tmp;
        if (temp != null && temp.length > 0) {
            // 复制上次未处理的数据 目前测试发现这块没有执行
            tmp = new byte[temp.length + len];
            System.arraycopy(temp, 0, tmp, 0, temp.length);
            System.arraycopy(data, 0, tmp, temp.length, len);
            temp = null;
        } else {
            tmp = new byte[len];
            System.arraycopy(data, 0, tmp, 0, len);
        }
        while (tmp != null && tmp.length > 0) {
            if (dataInfo == null) {
                // 如果数据头为空 tmp的长度大于等于数据头长度 则尝试获取数据头
                if (tmp.length >= DataInfo.LEN) {
                    // 将剩下的数据放在tmp中等待读取
                    // 如果获取数据头失败则抛出异常
                    tmp = getInfo(tmp, tmp.length);
                    cache = new byte[dataInfo.getLen()];
                } else {
                    // 如果tmp的长度比数据头短 则放在缓存等待下一次添加数据
                    temp = tmp;
                    tmp = null;
                }
            } else {
                int available = dataInfo.getLen() - pos;
                // 如果tmp长度比缓存剩余长度大 则能放多少放多少 剩下的放tmp里面
                if (tmp.length > available) {
                    System.arraycopy(tmp, 0, cache, pos, available);
                    pos += available;
                    temp = new byte[tmp.length - available];
                    System.arraycopy(tmp, available, temp, 0, temp.length);
                    tmp = temp;
                    temp = null;
                } else {
                    // 如果剩下的缓存刚好能放下 则都塞进去
                    System.arraycopy(tmp, 0, cache, pos, tmp.length);
                    pos += tmp.length;
                    tmp = null;
                }
                // 如果pos等于数据头中的长度 则处理数据
                if (pos == dataInfo.getLen()) {
                    dataInfo.setData(cache);
                    socket.receive(dataInfo);
                    dataInfo = null;
                    cache = null;
                    pos = 0;
                }
            }

        }

    }

    private byte[] getInfo(byte[] data, int len) throws DataFormatException {
        byte[] bHead = new byte[DataInfo.LEN];
        System.arraycopy(data, 0, bHead, 0, DataInfo.LEN);
        String strHead = new String(bHead);
        try {
            dataInfo = new DataInfo();
            // 压缩 2字节
            dataInfo.setCompress(Integer.parseInt(strHead.substring(0, 2), 16));
            // 长度 10字节
            dataInfo.setLen(Integer.parseInt(strHead.substring(2, 12)));
            byte[] tmp = new byte[len - DataInfo.LEN];
            System.arraycopy(data, DataInfo.LEN, tmp, 0, len - DataInfo.LEN);
            return tmp;
        } catch (NumberFormatException e) {
            throw new DataFormatException("意外的字符: " + e.getMessage());
        }
    }
}
