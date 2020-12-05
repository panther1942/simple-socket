package cn.erika.cli.service;

import cn.erika.aop.exception.BeanException;

import java.io.IOException;

public interface CliService {
    public void service(String[] args) throws IOException, BeanException;
}
