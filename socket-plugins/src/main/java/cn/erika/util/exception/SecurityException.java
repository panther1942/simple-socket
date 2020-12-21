package cn.erika.util.exception;

import java.io.IOException;

public class SecurityException extends IOException {
    public SecurityException() {
        super();
    }

    public SecurityException(String s) {
        super(s);
    }

    public SecurityException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SecurityException(Throwable throwable) {
        super(throwable);
    }
}
