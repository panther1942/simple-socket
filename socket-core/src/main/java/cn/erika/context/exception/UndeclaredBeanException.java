package cn.erika.context.exception;

public class UndeclaredBeanException extends BeanException {
    public UndeclaredBeanException() {
        super();
    }

    public UndeclaredBeanException(String s) {
        super(s);
    }

    public UndeclaredBeanException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
