package cn.erika.socket.core.tcp;

import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.Reader;
import cn.erika.socket.exception.DataFormatException;
import cn.erika.socket.model.pto.DataInfo;

public class DataReader implements Reader {
    private DataInfo dataInfo;
    private byte[] cache = new byte[0];

    @Override
    public void read(ISocket socket, byte[] data, int len) throws DataFormatException {
        byte[] tmp = new byte[cache.length + len];
        System.arraycopy(cache, 0, tmp, 0, cache.length);
        System.arraycopy(data, 0, tmp, cache.length, len);
        while (tmp != null && tmp.length > 0) {
            if (dataInfo == null) {
                // 如果数据头为空 tmp的长度大于等于数据头长度 则尝试获取数据头
                if (tmp.length >= DataInfo.LEN) {
                    // 将剩下的数据放在tmp中等待读取
                    // 如果获取数据头失败则抛出异常
                    tmp = getInfo(tmp, tmp.length);
                } else {
                    // 如果tmp的长度比数据头短 则放在缓存等待下一次添加数据
                    cache = tmp;
                    tmp = null;
                }
            } else {
                // 如果tmp中的数据长度达到了数据头中的长度 则交给上层处理
                if (tmp.length >= dataInfo.getLen()) {
                    byte[] tmp2 = new byte[dataInfo.getLen()];
                    System.arraycopy(tmp, 0, tmp2, 0, tmp2.length);
                    dataInfo.setData(tmp2);
                    socket.receive(dataInfo);
                    // 处理过的数据要从原来的缓存中去掉
                    dataInfo = null;
                    cache = new byte[0];
                    // 如果解析完的数据就是全部长度 则中断循环
                    if (tmp.length - tmp2.length == 0) {
                        break;
                    }
                    // 否则将剩下的放在tmp中继续循环
                    byte[] tmp3 = new byte[tmp.length - tmp2.length];
                    System.arraycopy(tmp, tmp2.length, tmp3, 0, tmp3.length);
                    tmp = tmp3;
                } else {
                    // 如果没达到数据头中的长度 就放在缓存中等待下次添加数据
                    cache = tmp;
                    tmp = null;
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
