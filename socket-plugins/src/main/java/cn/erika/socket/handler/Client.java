package cn.erika.socket.handler;

public interface Client {
    public void connect();

    public void send(String message);

    public void upload(String filepath, String filename);

    public void disconnect();
}
