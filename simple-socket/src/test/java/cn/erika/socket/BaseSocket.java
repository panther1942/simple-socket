package cn.erika.socket;

import cn.erika.socket.component.Message;
import cn.erika.socket.core.DataInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class BaseSocket {
    protected Map<String, Object> attr = new HashMap<>();
    private List<Task> taskList = new LinkedList<>();

    public abstract void send(Message message);

    public abstract void receive(DataInfo into);

    public abstract boolean isClosed();

    public abstract void close();

    public final void set(String key, Object value) {
        attr.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public final <T> T get(String key) {
        return (T) attr.get(key);
    }

    public final void remove(String key) {
        attr.remove(key);
    }

    public final void addTask(Task task){
        taskList.add(task);
    }
}
