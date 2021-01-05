package cn.erika.socket.orm.impl;

import cn.erika.context.annotation.Component;
import cn.erika.socket.model.po.Account;
import cn.erika.socket.orm.IAccountService;

import java.util.List;

@Component("accountService")
public class AccountServiceImpl implements IAccountService {
    private Account accountDao = Account.dao;

    @Override
    public List<Account> getList() {
        String sql = "SELECT * FROM tb_account";
        return accountDao.select(sql);
    }

    @Override
    public Account getByUuid(String uuid) {
        String sql = "SELECT * FROM tb_account WHERE `uuid`=?";
        return accountDao.selectOne(sql, uuid);
    }

    @Override
    public Account get4Auth(String username, String password) {
        String sql = "SELECT * FROM tb_account WHERE `username`=? AND `password`=?";
        return accountDao.selectOne(sql, username, password);
    }

    @Override
    public Account add(Account account) {
        if (account.insert() > 0) {
            return account;
        }
        return null;
    }

    @Override
    public Account changeAccountEnable(String uuid, boolean flag) {
        Account account = getByUuid(uuid);
        if (account != null) {
            account.setEnabled(flag);
            account.update();
            return account;
        }
        return null;
    }

    @Override
    public Account modify(Account account) {
        if (account.update() > 0) {
            return account;
        }
        return null;
    }

    @Override
    public Account remove(String uuid) {
        Account account = getByUuid(uuid);
        if (account != null && account.delete() > 0) {
            return account;
        }
        return null;
    }
}
