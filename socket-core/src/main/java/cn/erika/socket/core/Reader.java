package cn.erika.socket.core;

import cn.erika.socket.exception.DataFormatException;

public interface Reader {
    public void read(ISocket socket, byte[] data, int len) throws DataFormatException;
}
