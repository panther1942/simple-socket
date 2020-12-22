package cn.erika.socket.exception;

import java.io.IOException;

public class FileException extends IOException {
    public FileException(String s) {
        super(s);
    }

    public FileException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
