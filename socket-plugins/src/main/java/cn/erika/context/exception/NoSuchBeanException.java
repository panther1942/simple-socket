package cn.erika.context.exception;

public class NoSuchBeanException extends BeanException {
    public NoSuchBeanException() {
        super();
    }

    public NoSuchBeanException(String s) {
        super(s);
    }

    public NoSuchBeanException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NoSuchBeanException(Throwable throwable) {
        super(throwable);
    }
}
