package cn.erika.socket.exception;

public class AuthenticateException extends Exception {
    private static final long serialVersionUID = 1L;

    public AuthenticateException() {
        super();
    }

    public AuthenticateException(String s) {
        super(s);
    }

    public AuthenticateException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AuthenticateException(Throwable throwable) {
        super(throwable);
    }
}
