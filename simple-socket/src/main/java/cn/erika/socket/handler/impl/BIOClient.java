package cn.erika.socket.handler.impl;

import cn.erika.socket.core.TcpSocket;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BIOClient extends AbstractClientHandler {

    public BIOClient(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() {
        try {
            this.socket = new TcpSocket(address, this, CHARSET);
        } catch (IOException e) {
            onError(e.getMessage(), e);
        }
    }
}
