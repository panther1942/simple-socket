package cn.erika.cli.exception;

import cn.erika.context.exception.BeanException;

public class ClosedServerException extends BeanException {
    private static final long serialVersionUID = 1L;

    public ClosedServerException() {
        super("服务端未启动");
    }
}
