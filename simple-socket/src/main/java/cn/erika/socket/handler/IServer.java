package cn.erika.socket.handler;

public interface IServer extends Runnable {

    public void close();

    public void displayLink();

    public void send(String uid, String message);
}
