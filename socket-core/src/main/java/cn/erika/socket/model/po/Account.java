package cn.erika.socket.model.po;

import cn.erika.utils.db.Entry;
import cn.erika.utils.db.annotation.Column;
import cn.erika.utils.db.annotation.Table;
import cn.erika.utils.db.format.DateFormat;

import java.util.Date;

@Table("tb_account")
public class Account extends Entry<Account> {
    public static final Account dao = new Account();

    @Column(primary = true)
    private String uuid;
    private String username;
    private String password;
    private boolean enabled;
    @Column(value = "create_time", format = DateFormat.class)
    private Date createTime;
    @Column(value = "update_time", format = DateFormat.class)
    private Date updateTime;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public int insert() {
        this.createTime = new Date();
        this.updateTime = new Date();
        return super.insert();
    }

    @Override
    public int update() {
        this.updateTime = new Date();
        return super.update();
    }

    @Override
    public String toString() {
        return "Account{" +
                "uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
