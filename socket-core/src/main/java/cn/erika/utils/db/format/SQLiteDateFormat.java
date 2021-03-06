package cn.erika.utils.db.format;

import cn.erika.context.annotation.Component;
import cn.erika.utils.exception.EntryException;

import java.util.Date;

@Component
public class SQLiteDateFormat implements Format {

    @Override
    public <T> T format(Object obj) throws Throwable {
        if (obj != null) {
            if (obj instanceof Long) {
                return (T) new Date((long) obj);
            } else if (obj instanceof String) {
                return (T) new Date(Long.parseLong((String) obj));
            }else{
                throw new EntryException("无法转换的数据类型: " + obj.getClass().getName() + ":" + String.valueOf(obj));
            }
        }
        return null;
    }

    @Override
    public Object parse(Object obj) throws Throwable {
        if (obj != null) {
            Date date = (Date) obj;
            return date.getTime();
        }
        return null;
    }
}
