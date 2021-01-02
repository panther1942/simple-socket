package cn.erika.socket.exception;

public class DataFormatException extends Exception {
    private static final long serialVersionUID = 1L;

    public DataFormatException() {
        super();
    }

    public DataFormatException(String s) {
        super(s);
    }

    public DataFormatException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DataFormatException(Throwable throwable) {
        super(throwable);
    }
}
