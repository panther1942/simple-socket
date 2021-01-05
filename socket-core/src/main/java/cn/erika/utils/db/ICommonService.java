package cn.erika.utils.db;

import java.util.List;

public interface ICommonService<T extends Entry> {
    public List<T> getAll();

    public T getByUuid(String uuid);

    public T add(T t);

    public T modify(T t);

    public T remove(String uuid);
}
