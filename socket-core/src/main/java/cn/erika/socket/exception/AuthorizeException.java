package cn.erika.socket.exception;

public class AuthorizeException extends AuthenticateException {
    public AuthorizeException() {
        super();
    }

    public AuthorizeException(String s) {
        super(s);
    }

    public AuthorizeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AuthorizeException(Throwable throwable) {
        super(throwable);
    }
}
