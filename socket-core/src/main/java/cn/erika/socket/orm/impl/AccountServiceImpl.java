package cn.erika.socket.orm.impl;

import cn.erika.context.annotation.Component;
import cn.erika.socket.model.po.Account;
import cn.erika.socket.orm.IAccountService;
import cn.erika.utils.db.CommonServiceImpl;

@Component("accountService")
public class AccountServiceImpl extends CommonServiceImpl<Account> implements IAccountService {
    private Account accountDao = Account.dao;

    @Override
    public Account get4Auth(String username, String password) {
        String sql = "SELECT * FROM tb_account WHERE `username`=? AND `password`=?";
        return accountDao.selectOne(sql, username, password);
    }

    @Override
    public Account changeAccountEnable(String uuid, boolean flag) {
        Account account = getByUuid(uuid);
        if (account != null) {
            account.setEnabled(flag);
            return account;
        }
        return null;
    }
}
