package cn.erika.utils.exception;

public class NoSuchCompressAlgorithm extends Exception {
    private static final long serialVersionUID = 1L;

    public NoSuchCompressAlgorithm() {
        super();
    }

    public NoSuchCompressAlgorithm(String s) {
        super(s);
    }

    public NoSuchCompressAlgorithm(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NoSuchCompressAlgorithm(Throwable throwable) {
        super(throwable);
    }
}
