package cn.erika.socket.nio.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TcpChannel {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private SocketChannel channel;
    private Reader reader;
    private Handler handler;

    private Date startTime;
    private Map<String, Object> attr = new HashMap<>();

    public TcpChannel(SocketChannel channel, Handler handler, Charset charset) throws IOException {
        this.channel = channel;
        this.channel.configureBlocking(false);
        this.handler = handler;
        onEstablished();
    }

    public TcpChannel(InetSocketAddress address, Handler handler, Charset charset) throws IOException {
        this.channel = SocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.bind(address);
        if (this.channel.isConnectionPending()) {
            this.channel.finishConnect();
        }
        this.handler = handler;
        onEstablished();
    }

    private void onEstablished() throws IOException {
        this.startTime = new Date();
        this.handler.onEstablished(this);
    }

    public void read() {
        try {
            int cacheSize = channel.socket().getReceiveBufferSize();
            ByteBuffer buffer = ByteBuffer.allocate(cacheSize);
            try {
                if (channel.read(buffer) > 0) {
                    buffer.flip();
                    byte[] data = buffer.array();
                    reader.read(channel, data, data.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] data, int len) throws IOException {
        channel.write(ByteBuffer.wrap(data));
    }
}
