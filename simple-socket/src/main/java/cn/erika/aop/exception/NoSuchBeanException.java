package cn.erika.aop.exception;

public class NoSuchBeanException extends BeanException {
    public NoSuchBeanException() {
        super();
    }

    public NoSuchBeanException(String message) {
        super(message);
    }

    public NoSuchBeanException(String message, Throwable error) {
        super(message, error);
    }
}
