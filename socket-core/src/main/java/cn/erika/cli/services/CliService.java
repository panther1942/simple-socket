package cn.erika.cli.services;

import cn.erika.context.exception.BeanException;

public interface CliService {
    public void execute(String... args) throws BeanException;
}
