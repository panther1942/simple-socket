package cn.erika.utils.exception;

public class UnsupportedAlgorithmException extends Exception {
    private static final long serialVersionUID = 1L;

    public UnsupportedAlgorithmException() {
        super();
    }

    public UnsupportedAlgorithmException(String message) {
        super(message);
    }

    public UnsupportedAlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedAlgorithmException(Throwable cause) {
        super(cause);
    }
}
