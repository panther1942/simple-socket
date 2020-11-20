package cn.erika.test.cli.service;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {
    private static LinkedList<Class<? extends CliService>> serviceMap;
    private static ConcurrentHashMap<Class<?>, Object> objMap;

    public static void register(Class<? extends CliService> clazz) {
        if (serviceMap == null) {
            serviceMap = new LinkedList<>();
        }
        if (!serviceMap.contains(clazz)) {
            serviceMap.add(clazz);
        }
    }

    public static void add(Class<?> clazz, Object object) {
        if (objMap == null) {
            objMap = new ConcurrentHashMap<>();
        }
        objMap.put(clazz, object);
    }

    public static <T> T get(Class<?> clazz) {
        try {
            Object obj = objMap.get(clazz);
            if (obj == null) {
                if (CliService.class.isAssignableFrom(clazz)) {
                    obj = clazz.newInstance();
                    add(clazz, obj);
                    return (T) obj;
                } else {
                    return null;
                }
            } else {
                return (T) obj;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
