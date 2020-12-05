package cn.erika.socket.nio.core;

import java.nio.channels.SelectionKey;

public interface Handler {

    public void onAccept(SelectionKey selectionKey);

    public void onOpen(SelectionKey selectionKey);

    public void onEstablished(TcpChannel channel);

    public void onMessage(SelectionKey selectionKey);
}
