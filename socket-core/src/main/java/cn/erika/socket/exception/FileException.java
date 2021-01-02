package cn.erika.socket.exception;

import java.io.IOException;

public class FileException extends IOException {
    private static final long serialVersionUID = 1L;

    public FileException() {
        super();
    }

    public FileException(String s) {
        super(s);
    }

    public FileException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FileException(Throwable throwable) {
        super(throwable);
    }
}
