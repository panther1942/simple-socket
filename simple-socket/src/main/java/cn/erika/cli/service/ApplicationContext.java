package cn.erika.cli.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {
    private static ConcurrentHashMap<Class<?>, Object> objMap = new ConcurrentHashMap<>();

    private static Map<String, Class<? extends CliService>> serviceMap = new HashMap<>();
    private static Map<Class<? extends CliService>, CliService> serviceList = new HashMap<>();

    public static void register(Class<? extends CliService> clazz) throws IllegalAccessException, InstantiationException {
        serviceList.put(clazz, clazz.newInstance());
    }

    public static void register(String serviceName, Class<? extends CliService> clazz) {
        serviceMap.put(serviceName, clazz);
    }

    public static CliService getService(String serverName) {
        Class<? extends CliService> serviceClazz = serviceMap.get(serverName);
        if (serviceClazz != null) {
            return serviceList.get(serviceClazz);
        }
        return null;
    }

    public static void add(Class<?> clazz, Object object) {
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
