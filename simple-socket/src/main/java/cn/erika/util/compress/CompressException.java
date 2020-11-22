package cn.erika.util.compress;

public class CompressException extends Exception {
    public CompressException(String s) {
        super(s);
    }

    public CompressException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
