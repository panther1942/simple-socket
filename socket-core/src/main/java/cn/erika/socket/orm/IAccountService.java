package cn.erika.socket.orm;

import cn.erika.socket.model.po.Account;

import java.util.List;

public interface IAccountService {
    public List<Account> getList();

    public Account getByUuid(String uuid);

    public Account get4Auth(String username, String password);

    public Account add(Account account);

    public Account changeAccountEnable(String uuid, boolean flag);

    public Account modify(Account account);

    public Account remove(String uuid);
}
