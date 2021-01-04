package cn.erika.utils.exception;

public class EntryException extends Exception {
    public EntryException() {
        super();
    }

    public EntryException(String message) {
        super(message);
    }

    public EntryException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntryException(Throwable cause) {
        super(cause);
    }
}
