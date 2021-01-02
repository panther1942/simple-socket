package cn.erika.socket.core;

import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.core.component.Task;

import java.util.List;

public interface Handler {
    void init(ISocket socket);

    void onConnected(ISocket socket) throws BeanException;

    void onReady(ISocket socket);

    void onMessage(ISocket socket, Message message) throws BeanException;

    void onError(ISocket socket, Throwable throwable);

    void onClose(ISocket socket);

    void close();

    boolean isClosed();

    public List<Task> getTasks();

    public void addTask(Task task);

    public void delTask(Task task);

    public void emptyTasks();

    public void set(String key, Object value);

    public <T> T get(String key);

    public void remove(String key);

}
