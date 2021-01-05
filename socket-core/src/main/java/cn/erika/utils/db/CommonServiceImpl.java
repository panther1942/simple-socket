package cn.erika.utils.db;


import cn.erika.utils.db.annotation.Table;
import cn.erika.utils.exception.EntryException;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class CommonServiceImpl<T extends Entry> implements ICommonService<T> {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getAll() {
        Class<T> clazz = getTClass();
        Table table = clazz.getAnnotation(Table.class);
        try {
            if (table == null) {
                throw new EntryException("缺少表名");
            }
            String sql = "SELECT * FROM " + table.value();
            T t = getDao(clazz);
            if (t == null) {
                t = clazz.newInstance();
            }
            return t.select(sql);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getByUuid(String uuid) {
        Class<T> clazz = getTClass();
        Table table = clazz.getAnnotation(Table.class);
        try {
            if (table == null) {
                throw new EntryException("缺少表名");
            }
            String sql = "SELECT * FROM " + table.value() + " WHERE `uuid`=?";
            T t = getDao(clazz);
            if (t == null) {
                t = clazz.newInstance();
            }
            try {
                clazz.getDeclaredField("uuid");
            } catch (NoSuchFieldException e) {
                throw new EntryException("缺少uuid字段");
            }
            return (T) t.selectOne(sql, uuid);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public T add(T t) {
        if (t.insert() > 0) {
            return t;
        }
        return null;
    }

    @Override
    public T modify(T t) {
        if (t.update() > 0) {
            return t;
        }
        return null;
    }

    @Override
    public T remove(String uuid) {
        T t = getByUuid(uuid);
        if (t != null && t.delete() > 0) {
            return t;
        }
        return null;
    }

    private Class<T> getTClass() {
        Class<T> tClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return tClass;
    }

    private T getDao(Class<T> clazz) {
        try {
            T t = null;
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Object dao = field.get(null);
                if (dao != null && clazz.isInstance(dao)) {
                    return (T) dao;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
