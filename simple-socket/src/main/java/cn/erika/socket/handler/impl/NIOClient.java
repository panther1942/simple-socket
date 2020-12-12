package cn.erika.socket.handler.impl;

import cn.erika.socket.core.TcpChannel;

import java.io.IOException;
import java.net.InetSocketAddress;

public class NIOClient extends AbstractClientHandler {

    public NIOClient(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() {
        try {
            this.socket = new TcpChannel(address, this, CHARSET);
        } catch (IOException e) {
            onError(e.getMessage(), e);
        }
    }
}
