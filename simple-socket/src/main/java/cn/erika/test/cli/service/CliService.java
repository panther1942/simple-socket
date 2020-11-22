package cn.erika.test.cli.service;

import java.net.SocketException;

public interface CliService {
    public void service(String[] args) throws SocketException;
}
