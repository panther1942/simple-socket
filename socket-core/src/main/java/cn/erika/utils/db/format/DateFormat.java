package cn.erika.utils.db.format;

import cn.erika.context.annotation.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateFormat implements Format {
    // Sqlite没有Date类型 存进去的实际上是格式化后的字符串 取出来需要格式化
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public <T> T format(Object obj) throws Throwable {
        if (obj != null) {
            if(obj instanceof String){
                String date = (String) obj;
                return (T) sdf.parse(date);
            }else{
                return (T) obj;
            }
        }
        return null;
    }

    @Override
    public Object parse(Object obj) throws Throwable {
        if (obj != null) {
            Date date = (Date) obj;
            return sdf.format(date);
        }
        return null;
    }
}
