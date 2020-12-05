package cn.erika.aop.exception;

public class BeanException extends Exception {
    private static final long serialVersionUID = 1L;

    public BeanException() {
        super();
    }

    public BeanException(String message) {
        super(message);
    }

    public BeanException(String message, Throwable error) {
        super(message, error);
    }
}
