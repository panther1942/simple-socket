package cn.erika.aop.exception;

public class BeanException extends Exception {
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
