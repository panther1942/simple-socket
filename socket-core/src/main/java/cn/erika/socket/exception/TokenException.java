package cn.erika.socket.exception;

public class TokenException extends AuthenticateException {
    private static final long serialVersionUID = 1L;

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
