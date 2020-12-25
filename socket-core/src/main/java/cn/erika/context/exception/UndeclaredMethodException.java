package cn.erika.context.exception;

public class UndeclaredMethodException extends NoSuchMethodException {
    public UndeclaredMethodException() {
        super();
    }

    public UndeclaredMethodException(String s) {
        super(s);
    }
}
