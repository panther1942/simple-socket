package cn.erika.context;

import cn.erika.context.annotation.Component;
import cn.erika.context.exception.BeanException;
import cn.erika.context.exception.NoSuchBeanException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {
    private static BeanFactory factory = new BeanFactory();
    private Map<Class<?>, Object> beanList = new ConcurrentHashMap<>();
    private Map<String, Class<?>> aliasList = new HashMap<>();
    private List<Class<?>> exclusionBean = new LinkedList<>();

    private BeanFactory() {
    }

    public static BeanFactory getInstance() {
        if (factory == null) {
            factory = new BeanFactory();
        }
        return factory;
    }

    public void addBean(Class<?> clazz, Object obj) {
        beanList.put(clazz, obj);
    }

    public void addBean(String name, Class<?> clazz) {
        aliasList.put(name, clazz);
    }

    // 获取Bean并做好类型转换
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
                throw new NoSuchBeanException("不存在类型为: " + clazz.getName() + " 的bean");
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
    private <T> T createBean(Class<?> clazz) throws BeanException {
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
            throw new BeanException("缺少无参构造函数");
        } catch (IllegalAccessException e) {
            throw new BeanException("构造函数无法访问");
        }
    }

    public boolean existBean(Class<?> clazz) {
        return beanList.containsKey(clazz);
    }

    // 排除列表中的bean不会被创建 只能手动创建并添加到列表中
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
}
