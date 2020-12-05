package cn.erika.socket.nio.core;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

public abstract class AbstractHandler implements Handler,Runnable {
    private Selector selector;
    protected Charset charset = Charset.forName("UTF-8");

    public AbstractHandler() throws IOException {
        this.selector = Selector.open();
    }

    public void register(SelectableChannel channel, int status) throws ClosedChannelException {
        channel.register(selector, status);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (selector.select() > 0) {
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (key.isConnectable()) {
                            onOpen(key);
                        }
                        if (key.isAcceptable()) {
                            onAccept(key);
                        }
                        if (key.isReadable()) {
                            onMessage(key);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
