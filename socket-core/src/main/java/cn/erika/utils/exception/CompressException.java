package cn.erika.utils.exception;

public class CompressException extends Exception {
    private static final long serialVersionUID = 1L;

    public CompressException() {
        super();
    }

    public CompressException(String s) {
        super(s);
    }

    public CompressException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CompressException(Throwable throwable) {
        super(throwable);
    }
}
