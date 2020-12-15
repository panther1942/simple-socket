package cn.erika.socket.exception;

public class FileException extends Exception {
    public FileException(String s) {
        super(s);
    }

    public FileException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
