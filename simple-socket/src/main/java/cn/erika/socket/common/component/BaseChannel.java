package cn.erika.socket.common.component;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;

public interface BaseChannel extends BaseSocket {
    public void register(Selector selector, int selectorStatus) throws ClosedChannelException;
}
