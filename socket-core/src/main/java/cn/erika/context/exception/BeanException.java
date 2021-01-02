package cn.erika.context.exception;

public class BeanException extends Exception {
    private static final long serialVersionUID = 1L;

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
