package cn.erika.cli.services;

import cn.erika.context.exception.BeanException;

public interface CliService {
    public String info();

    public void execute(String... args) throws BeanException;

}
