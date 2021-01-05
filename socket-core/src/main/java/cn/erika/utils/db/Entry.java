package cn.erika.utils.db;

import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.utils.db.annotation.Column;
import cn.erika.utils.db.annotation.Table;
import cn.erika.utils.exception.EntryException;
import cn.erika.utils.db.format.Format;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.string.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Entry<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private BeanFactory beanFactory = BeanFactory.getInstance();
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public final List<T> select(String sql, Object... params) {
        log.debug(sql);
        log.debug(StringUtils.join(",", params).toString());
        JdbcUtils utils = JdbcUtils.getInstance();
        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet result = null;
        List<T> resultList = new LinkedList<>();
        try {
            conn = utils.getConn();
            pStmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    pStmt.setObject(i + 1, params[i]);
                }
            }
            result = pStmt.executeQuery();
            ResultSetMetaData meta = result.getMetaData();
            int columnCount = meta.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = meta.getColumnName(i + 1);
            }
            while (result.next()) {
                Object[] dataArray = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    dataArray[i] = result.getObject(i + 1);
                }
                resultList.add(convertArray2Entry(columnNames, dataArray));
            }
        } catch (SQLException e) {
            log.error("数据查询出错: " + e.getMessage(), e);
        } catch (EntryException e) {
            log.error(e.getMessage());
        } finally {
            utils.close(conn, pStmt, result);
        }
        return resultList;
    }

    public final T selectOne(String sql, Object... params) {
        List<T> list = select(sql, params);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private T select() {
        try {
            Table table = this.getClass().getAnnotation(Table.class);
            if (table == null || StringUtils.isEmpty(table.value())) {
                throw new EntryException("缺少表名");
            }
            StringBuffer buffer = new StringBuffer("SELECT * FROM ");
            buffer.append(table.value()).append(" WHERE ");
            Map<String, Object> fields = convertEntry2Map(this);
            List<Object> params = new LinkedList<>();
            String primary = (String) fields.get("primary");
            for (String key : fields.keySet()) {
                if (key.equals("primary") || key.equals(primary)) {
                    continue;
                }
                if (fields.get(key) == null) {
                    continue;
                }
                buffer.append("`").append(key).append("`=? AND");
                params.add(fields.get(key));
            }
            buffer.delete(buffer.length() - 4, buffer.length());
            return selectOne(buffer.toString(), params.toArray());
        } catch (EntryException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public final int insert() {
        JdbcUtils utils = JdbcUtils.getInstance();
        try {
            Table table = this.getClass().getAnnotation(Table.class);
            if (StringUtils.isEmpty(table.value())) {
                throw new EntryException("无法确定表名");
            }
            Map<String, Object> params = convertEntry2Map(this);
            String[] columnNames = new String[params.size() - 1];
            Object[] dataArray = new Object[params.size() - 1];
            int count = 0;
            for (String key : params.keySet()) {
                if ("primary".equals(key)) {
                    continue;
                }
                columnNames[count] = key;
                dataArray[count] = params.get(key);
                count++;
            }
            StringBuffer buffer = new StringBuffer("INSERT INTO ");
            buffer.append(table.value()).append(" (");
            for (String column : columnNames) {
                buffer.append("`").append(column).append("`,");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append(") VALUES (");
            for (int i = 0; i < columnNames.length; i++) {
                buffer.append("?,");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append(")");
            int result = utils.update(buffer.toString(), dataArray);
            if (result > 0) {
                flushFields(select());
                return result;
            }
        } catch (EntryException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    public final int update() {
        JdbcUtils utils = JdbcUtils.getInstance();
        try {
            Table table = this.getClass().getAnnotation(Table.class);
            if (StringUtils.isEmpty(table.value())) {
                throw new EntryException("无法确定表名");
            }
            Map<String, Object> params = convertEntry2Map(this);
            String primary = (String) params.get("primary");
            String[] columnNames = new String[params.size() - 2];
            Object[] dataArray = new Object[params.size() - 2];
            int count = 0;
            for (String key : params.keySet()) {
                if (key.equals(primary) || "primary".equals(key)) {
                    continue;
                }
                columnNames[count] = key;
                dataArray[count] = params.get(key);
                count++;
            }
            StringBuffer buffer = new StringBuffer("UPDATE ");
            buffer.append(table.value()).append(" SET ");
            for (String column : columnNames) {
                if (column.equals(primary)) {
                    continue;
                }
                buffer.append("`").append(column).append("`=?,");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append(" WHERE ");
            buffer.append("`").append(primary).append("`='").append(params.get(primary)).append("'");
            int result = utils.update(buffer.toString(), dataArray);
            if (result > 0) {
                flushFields(select());
                return result;
            }
        } catch (EntryException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    public final int delete() {
        JdbcUtils utils = JdbcUtils.getInstance();
        try {
            Table table = this.getClass().getAnnotation(Table.class);
            if (StringUtils.isEmpty(table.value())) {
                throw new EntryException("无法确定表名");
            }
            T target = select();
            Map<String, Object> params = convertEntry2Map(this);
            String primary = (String) params.get("primary");
            StringBuffer buffer = new StringBuffer("DELETE FROM ");
            buffer.append(table.value()).append(" WHERE `");
            buffer.append(primary).append("`=?");
            int result = utils.update(buffer.toString(), params.get(primary));
            if (result > 0) {
                flushFields(target);
                return result;
            }
        } catch (EntryException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    private Map<String, Object> convertEntry2Map(Object obj) throws EntryException {
        Map<String, Object> valueMap = new HashMap<>();
        Method[] methods = obj.getClass().getDeclaredMethods();
        try {
            for (Method method : methods) {
                if (method.getName().startsWith("get")
                        && method.getParameterCount() == 0) {
                    Object value = null;
                    try {
                        value = method.invoke(obj);
                    } catch (IllegalAccessException e) {
                        continue;
                    }

                    String name = method.getName();
                    name = name.substring("get".length());
                    char firstChar = name.charAt(0);
                    if (firstChar > 64 && firstChar < 91) {
                        firstChar += 32;
                    }
                    name = firstChar + name.substring(1);
                    Field field = null;
                    try {
                        field = obj.getClass().getDeclaredField(name);
                    } catch (NoSuchFieldException e) {
                        continue;
                    }
                    Column column = field.getAnnotation(Column.class);
                    if (column != null && !StringUtils.isEmpty(column.value())) {
                        name = column.value();
                    }
                    if (column != null && column.primary()) {
                        valueMap.put("primary", name);
                    }
                    if (column != null && !Void.class.equals(column.format())) {
                        Class<?> formatClass = column.format();
                        Format format = beanFactory.getBean(formatClass);
                        value = format.parse(value);
                    }
                    if (value == null) {
                        continue;
                    }
                    valueMap.put(name, value);
                }
            }
            return valueMap;
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            throw new EntryException("get方法抛出异常: " + e.getTargetException().getMessage());
        } catch (BeanException e) {
            throw new EntryException("找不到指定的格式化方法: " + e.getMessage());
        } catch (Throwable throwable) {
            throw new EntryException("格式化异常: " + throwable.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private T convertArray2Entry(String[] fieldList, Object[] dataList) throws EntryException {
        try {
            T t = (T) this.getClass().newInstance();
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                Column column = field.getAnnotation(Column.class);
                int index;
                if (column != null && !StringUtils.isEmpty(column.value())) {
                    index = getIndex(column.value(), fieldList);
                } else {
                    index = getIndex(field.getName(), fieldList);
                }
                if (index == -1) {
                    continue;
                }
                field.setAccessible(true);
                if (column != null && !Void.class.equals(column.format())) {
                    Class<?> formatClass = column.format();
                    Format format = beanFactory.getBean(formatClass);
                    field.set(t, format.format(dataList[index]));
                } else {
                    if (field.getType().equals(String.class)) {
                        field.set(t, String.valueOf(dataList[index]));
                    } else {
                        field.set(t, dataList[index]);
                    }
                }
            }
            return t;
        } catch (InstantiationException e) {
            throw new EntryException("实体类无法实例化: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new EntryException("实体类的无参构造方法无法访问");
        } catch (BeanException e) {
            throw new EntryException("找不到指定的格式化方法: " + e.getMessage());
        } catch (Throwable throwable) {
            throw new EntryException("格式化异常: " + throwable.getMessage());
        }
    }

    private int getIndex(Object target, Object[] array) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        return -1;
    }

    private void flushFields(T data) throws EntryException {
        if (!this.getClass().isInstance(data)) {
            throw new EntryException("不匹配的类型");
        }
        try {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(data);
                field.set(this, value);
            }
        } catch (IllegalAccessException e) {
            throw new EntryException("刷新数据失败", e);
        }
    }
}
