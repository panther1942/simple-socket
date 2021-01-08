package cn.erika.socket.exception;

public class LimitThreadException extends Exception {
    public LimitThreadException() {
        super();
    }

    public LimitThreadException(String message) {
        super(message);
    }

    public LimitThreadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LimitThreadException(Throwable cause) {
        super(cause);
    }
}
