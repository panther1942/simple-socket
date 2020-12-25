package cn.erika.context.exception;

public class BeanException extends Exception {
    public BeanException() {
        super();
    }

    public BeanException(String s) {
        super(s);
    }

    public BeanException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BeanException(Throwable throwable) {
        super(throwable);
    }
}
