package cn.erika.cli.service;

import cn.erika.aop.exception.BeanException;

public interface CliService {
    public void service(String[] args) throws BeanException;
}
