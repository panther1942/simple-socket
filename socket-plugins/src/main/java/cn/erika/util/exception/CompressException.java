package cn.erika.util.exception;

public class CompressException extends Exception {
    public CompressException(String s) {
        super(s);
    }

    public CompressException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
