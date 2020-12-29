package cn.erika.socket.exception;

public class AuthenticateException extends Exception {
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
