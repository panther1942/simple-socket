package cn.erika.utils.db.format;

import cn.erika.context.annotation.Component;
import cn.erika.utils.exception.EntryException;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class MySQLDateFormat implements Format {

    @Override
    public <T> T format(Object obj) throws Throwable {
        if (obj != null) {
            if (obj instanceof java.sql.Timestamp || obj instanceof java.sql.Date) {
                return (T) obj;
            } else {
                throw new EntryException("无法转换的数据类型: " + obj.getClass().getName() + ":" + String.valueOf(obj));
            }
        }
        return null;
    }

    @Override
    public Object parse(Object obj) throws Throwable {
        return obj;
    }
}
