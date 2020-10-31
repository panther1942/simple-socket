package cn.erika.util.security;

public class SecurityException extends Exception {
    public SecurityException() {
        super();
    }

    public SecurityException(String s) {
        super(s);
    }

    public SecurityException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
