package cn.erika.socket.orm;

import cn.erika.socket.model.po.Account;
import cn.erika.utils.db.ICommonService;

public interface IAccountService extends ICommonService<Account> {

    public Account get4Auth(String username, String password);

    public Account changeAccountEnable(String uuid, boolean flag);
}
