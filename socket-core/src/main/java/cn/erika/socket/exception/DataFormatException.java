package cn.erika.socket.exception;

public class DataFormatException extends Exception {
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

    protected DataFormatException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
