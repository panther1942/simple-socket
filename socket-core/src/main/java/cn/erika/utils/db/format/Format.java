package cn.erika.utils.db.format;

public interface Format {
    public <T> T format(Object obj) throws Throwable;

    public Object parse(Object obj) throws Throwable;
}
