package cn.erika.context.exception;

public class UndeclaredMethodException extends NoSuchMethodException {
    private static final long serialVersionUID = 1L;

    public UndeclaredMethodException() {
        super();
    }

    public UndeclaredMethodException(String s) {
        super(s);
    }
}
