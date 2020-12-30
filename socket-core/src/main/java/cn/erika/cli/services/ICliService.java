package cn.erika.cli.services;

import cn.erika.context.exception.BeanException;

public interface ICliService {
    String info();

    void execute(String... args) throws BeanException;

}
