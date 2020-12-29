package cn.erika.socket.exception;

public class TokenException extends AuthenticateException {
    public TokenException() {
        super();
    }

    public TokenException(String s) {
        super(s);
    }

    public TokenException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TokenException(Throwable throwable) {
        super(throwable);
    }
}
