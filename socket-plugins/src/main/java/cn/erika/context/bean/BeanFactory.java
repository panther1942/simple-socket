package cn.erika.context.bean;

import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.context.exception.NoSuchBeanException;
import jdk.internal.dynalink.support.ClassMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {
    private static BeanFactory factory = new BeanFactory();
    private Map<Class<?>, Object> beanList = new ConcurrentHashMap<>();
    private Map<String, Class<?>> aliasList = new HashMap<>();
    private Map<String, Method> serviceList = new HashMap<>();
    private List<Class<?>> exclusionBean = new LinkedList<>();

    private BeanFactory() {
    }

    public static BeanFactory getInstance() {
        if (factory == null) {
            factory = new BeanFactory();
        }
        return factory;
    }

    public void addBean(Class<?> clazz, Object object) {
        beanList.put(clazz, object);
    }

    public void addBean(String name, Class<?> clazz) {
        aliasList.put(name, clazz);
    }

    public void addBean(String name, Method method) {
        serviceList.put(name, method);
    }

    // 通过类名获取单实例对象
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<?> clazz) throws BeanException {
        T bean = null;
        if (beanList.containsKey(clazz)) {
            Object obj = beanList.get(clazz);
            try {
                bean = (T) obj;
            } catch (ClassCastException e) {
                throw new BeanException("类型: " + obj.getClass().getName() + "与预期" + clazz.getName() + "不匹配", e);
            }
            return bean;
        } else {
            if (exclusionBean.contains(clazz)) {
                return null;
                //                throw new NoSuchBeanException("不存在类型为: " + clazz.getName() + " 的bean");
            } else {
                bean = createBean(clazz);
                Component component = clazz.getAnnotation(Component.class);
                if (component != null) {
                    if (Component.Type.SingleTon.equals(component.type())) {
                        beanList.put(clazz, bean);
                    }
                }
                return bean;
            }
        }
    }

    // 通过服务名称查找服务类对象
    public <T> T getBean(String name) throws BeanException {
        List<String> target = new LinkedList<>();
        for (String alia : aliasList.keySet()) {
            if (alia.startsWith(name)) {
                target.add(alia);
            }
        }
        if (target.size() == 0) {
            throw new NoSuchBeanException("未找到名称为: " + name + " 的bean");
        } else if (target.size() > 1) {
            StringBuffer buffer = new StringBuffer("不明确的服务: ");
            for (String srvName : target) {
                buffer.append(srvName);
                buffer.append(",");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            throw new BeanException(buffer.toString());
        } else {
            return getBean(aliasList.get(target.get(0)));
        }
    }

    // 创建目标Bean
    @SuppressWarnings("unchecked")
    public <T> T createBean(Class<?> clazz) throws BeanException {
        try {
            Object target = clazz.newInstance();
            InvocationProxy proxy = new InvocationProxy(target);
            if (target.getClass().getInterfaces().length == 0) {
                throw new BeanException("代理类必须实现一个接口，且调用该类的方法必须在接口中声明");
            }
            return (T) Proxy.newProxyInstance(
                    target.getClass().getClassLoader(),
                    target.getClass().getInterfaces(),
                    proxy
            );
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new BeanException("缺少无参构造函数");
        } catch (IllegalAccessException e) {
            throw new BeanException("构造函数无法访问");
        }
    }

    private List<Class<?>> getInterfaces(Class<?> clazz) {
        List<Class<?>> interfaces = new LinkedList<>();
        if (Object.class.equals(clazz)) {
            return interfaces;
        }
        interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
        interfaces.addAll(getInterfaces(clazz.getSuperclass()));
        return interfaces;
    }

    public boolean existBean(Class<?> clazz) {
        return beanList.containsKey(clazz);
    }

    // 排除列表中的bean不会自动创建 只能手动创建并添加到列表中
    public void excludeBean(Class<?> clazz) {
        exclusionBean.add(clazz);
    }

    // 反射处理器 在这里做了增强Advice
    // 搞成静态内部类是因为只有创建Bean的时候用 而且不想让外部访问
    private class InvocationProxy implements InvocationHandler {
        // 代理的目标
        private Object target;

        private InvocationProxy(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    public Object execute(BeanSelector handler, String name, Object... args) throws BeanException {
        Class<?> clazz = aliasList.get(name);
        Object object = getBean(clazz);
        Method method = null;
        try {
            method = handler.getMethod(clazz);
        } catch (NoSuchMethodException e) {
            throw new BeanException("目标方法不存在: " + name);
        }
        try {
            return Proxy.getInvocationHandler(object).invoke(object, method, args);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new BeanException("内部错误: " + throwable.getMessage());
        }
    }

    public Object execute(String name, Object... args) throws BeanException {
        Method method = serviceList.get(name);
        Class<?> clazz = method.getDeclaringClass();
        Object object = getBean(clazz);
        try {
            return Proxy.getInvocationHandler(object).invoke(object, method, args);
        } catch (Throwable throwable) {
            throw new BeanException("内部异常", throwable);
        }
    }
}
