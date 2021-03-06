package cn.erika.utils.exception;

import java.io.IOException;

public class SerialException extends IOException {
    private static final long serialVersionUID = 1L;

    public SerialException() {
        super();
    }

    public SerialException(String s) {
        super(s);
    }

    public SerialException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SerialException(Throwable throwable) {
        super(throwable);
    }
}
