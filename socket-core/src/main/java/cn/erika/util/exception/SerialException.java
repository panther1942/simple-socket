package cn.erika.util.exception;

public class SerialException extends Exception {
    public SerialException() {
        super();
    }

    public SerialException(String s) {
        super(s);
    }

    public SerialException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
