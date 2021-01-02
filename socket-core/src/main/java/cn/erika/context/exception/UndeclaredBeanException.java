package cn.erika.context.exception;

public class UndeclaredBeanException extends BeanException {
    private static final long serialVersionUID = 1L;

    public UndeclaredBeanException() {
        super();
    }

    public UndeclaredBeanException(String s) {
        super(s);
    }

    public UndeclaredBeanException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UndeclaredBeanException(Throwable throwable) {
        super(throwable);
    }
}
