package cn.erika.socket.exception;

public class ServiceException extends Exception {
    private static final long serialVersionUID = 1L;

    public ServiceException() {
        super();
    }

    public ServiceException(String s) {
        super(s);
    }

    public ServiceException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ServiceException(Throwable throwable) {
        super(throwable);
    }
}
