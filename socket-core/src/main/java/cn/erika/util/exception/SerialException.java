package cn.erika.util.exception;

import java.io.IOException;

public class SerialException extends IOException {

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
