package cn.erika.socket;

import cn.erika.socket.component.Message;

public interface Handler {
    public void init(BaseSocket socket);

    public void onOpened(BaseSocket socket);

    public void onReady(BaseSocket socket);

    public void onMessage(BaseSocket socket, Message message);

    public void onClosed(BaseSocket socket);
}
