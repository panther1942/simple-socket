package cn.erika.socket.common.exception;

public class FileException extends Exception {
    public FileException(String s) {
        super(s);
    }

    public FileException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
