package cn.erika.utils.exception;

public class NoSuchCompressAlgorithm extends Exception {
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

    protected NoSuchCompressAlgorithm(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
