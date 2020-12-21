package cn.erika.socket;

import cn.erika.socket.component.Message;
import cn.erika.socket.core.DataInfo;

import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;

public class TcpSocket extends BaseSocket {
    private Charset charset;
    private SocketAddress address;
    private Socket socket;
    private Handler handler;
    private Reader reader;

    public TcpSocket(SocketAddress address, Handler handler) {
        this.address = address;
        this.socket = new Socket();
    }

    @Override
    public void send(Message message) {

    }

    @Override
    public void receive(DataInfo into) {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }
}
