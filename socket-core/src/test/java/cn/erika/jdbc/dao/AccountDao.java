package cn.erika.jdbc.dao;

import cn.erika.jdbc.model.Account;

import java.util.List;

public interface AccountDao {
    public List<Account> getAccountList();
}
