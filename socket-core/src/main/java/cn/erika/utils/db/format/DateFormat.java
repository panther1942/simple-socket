package cn.erika.utils.db.format;

import cn.erika.context.annotation.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateFormat implements Format {

    @Override
    public <T> T format(Object obj) throws Throwable {
        if (obj != null) {
            return (T) new Date((long) obj);
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
