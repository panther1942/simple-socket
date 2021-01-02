package cn.erika.socket.core.component;

import cn.erika.socket.core.ISocket;

public abstract class Task implements Runnable {
    protected ISocket socket;

    public ISocket getSocket() {
        return socket;
    }

    public void setSocket(ISocket socket) {
        this.socket = socket;
    }
}
