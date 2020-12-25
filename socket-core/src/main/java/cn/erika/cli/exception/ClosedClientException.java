package cn.erika.cli.exception;

import cn.erika.context.exception.BeanException;

public class ClosedClientException extends BeanException {
    public ClosedClientException() {
        super("客户端未启动");
    }
}
