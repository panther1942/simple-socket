package cn.erika.context.bean;

import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.context.exception.BeanException;
import cn.erika.context.exception.NoSuchBeanException;
import cn.erika.context.exception.UndeclaredBeanException;
import cn.erika.context.exception.UndeclaredMethodException;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactory {
    // 单例工厂
    private static BeanFactory factory = new BeanFactory();
    // 存储单例对象
    private Map<Class<?>, Object> beanList = new ConcurrentHashMap<>();
    // 存储服务别名 粒度为Class
    private Map<String, Class<?>> aliasList = new HashMap<>();
    // 存储服务别名 粒度为Method
    private Map<String, Method> serviceList = new HashMap<>();

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

    public <T> Map<String, T> getBeans(Class<T> clazz) throws BeanException {
        Map<String, T> beanList = new HashMap<>();
        for (String name : aliasList.keySet()) {
            if (clazz.isAssignableFrom(aliasList.get(name))) {
                beanList.put(name, getBean(aliasList.get(name)));
            }
        }
        return beanList;
    }

    // 通过类名获取单实例对象
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<?> clazz) throws BeanException {
        T bean = null;
        // 如果已经缓存对象 则直接返回
        if (beanList.containsKey(clazz)) {
            Object obj = beanList.get(clazz);
            try {
                // 必须要检查转换异常 否则就算这里不报错 运行时也会报错
                bean = (T) obj;
            } catch (ClassCastException e) {
                throw new BeanException("类型: " + obj.getClass().getName() + "与预期" + clazz.getName() + "不匹配", e);
            }
            return bean;
        } else {
            // 如果没有缓存对象 则尝试创建对象 这需要一个无参可访问的构造函数
            Component component = clazz.getAnnotation(Component.class);
            if (component != null && !component.ignore()) {
                // 如果clazz具有Component注解 而且不被忽略 则创建对象 否则忽略返回NULL
                bean = createBean(clazz);
                // 如果是单例 则缓存对象
                if (Component.Type.SingleTon.equals(component.type())) {
                    beanList.put(clazz, bean);
                }
            }
            return bean;
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
        // 这里需要检查匹配的条目数量 实现模糊匹配 首字母开始
        if (target.size() == 0) {
            throw new NoSuchBeanException("未找到名称为: " + name + " 的bean");
        } else if (target.size() > 1) {
            StringBuffer buffer = new StringBuffer("不明确的服务: ");
            for (String srvName : target) {
                buffer.append(srvName).append(",");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            throw new UndeclaredBeanException(buffer.toString());
        } else {
            return getBean(aliasList.get(target.get(0)));
        }
    }

    public <T> T createBean(Class<?> clazz, Object... args) throws BeanException {
        try {
            Object obj;
            if (args != null && args.length > 0) {
                Class<?>[] paramTypes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    paramTypes[i] = args[i].getClass();
                }
                Constructor<?> con = checkConstructor(clazz, paramTypes);
                if (con != null) {
                    obj = con.newInstance(args);
                } else {
                    throw new NoSuchMethodException();
                }
            } else {
                obj = clazz.newInstance();
            }
            return getProxy(obj);
        } catch (NoSuchMethodException | InstantiationException e) {
            throw new BeanException("不存在这样的构造函数");
        } catch (IllegalAccessException e) {
            throw new BeanException("构造函数无法访问");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new BeanException("底层构造方法抛出异常");
        }
    }

    private Constructor<?> checkConstructor(Class<?> clazz, Class<?>[] paramTypes) {
        Constructor<?>[] constructors = clazz.getConstructors();
        boolean flag = false;
        for (Constructor<?> con : constructors) {
            Class<?>[] srcTypes = con.getParameterTypes();
            if (srcTypes.length == paramTypes.length) {
                for (int i = 0; i < srcTypes.length; i++) {
                    if (checkType(srcTypes[i], paramTypes[i])) {
                        flag = true;
                    } else {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    return con;
                }
            }
        }
        return null;
    }

    private boolean checkType(Class<?> src, Class<?> dest) {
        if (dest == null) {
            // 如果传入的参数部分为空 则匹配这个位置任意类型的参数
            return true;
        } else if (src.isAssignableFrom(dest)) {
            // 如果参数类型匹配 或为其子类
            return true;
        } else // 如果既不为空 且参数类型又不匹配
            return src.isPrimitive() && src.getName().equals(dest.getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    private <T> T getProxy(Object object) throws BeanException {
        InvocationProxy proxy = new InvocationProxy(object);
        if (object.getClass().getInterfaces().length == 0) {
            throw new BeanException("代理类必须实现一个接口，且调用该类的方法必须在接口中声明");
        }
        return (T) Proxy.newProxyInstance(
                object.getClass().getClassLoader(),
                object.getClass().getInterfaces(),
                proxy
        );
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
            Object result = null;
            Advise advise = null;
            // 检查被代理的目标是不是增强器的实现类 如果是的话 跳过AOP检测
            if (!Advise.class.isInstance(target)) {
                // 获取所有型参的类型
                Class<?>[] typeArray = null;
                if (args != null) {
                    typeArray = new Class<?>[args.length];
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] != null) {
                            typeArray[i] = args[i].getClass();
                        } else {
                            typeArray[i] = null;
                        }
                    }
                }
                // 获取目标方法
                Method targetMethod = getMethod(method.getName(), typeArray);
                // 检查方法上的注解
                Enhance enhance = targetMethod.getAnnotation(Enhance.class);
                // 如果存在Aspect注解 则获取增强类的实例
                if (enhance != null) {
                    advise = getBean(enhance.value());
                }
            }
            try {
                // 如果增强器不为空 则执行增强部分的方法 否则执行目标方法
                if (advise != null) {
                    try {
                        advise.before(method, args);
                        result = method.invoke(target, args);
                        advise.afterReturning(method, args, result);
                        return result;
                    } catch (Throwable t) {
                        advise.afterThrowing(method, args, t);
                        throw t;
                    }
                } else {
                    return method.invoke(target, args);
                }
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        private Method getMethod(String name, Class<?>... argTypes) throws NoSuchMethodException {
            List<Method> targetMethod = new LinkedList<>();
            Method[] methods = target.getClass().getMethods();
            boolean flag = false;
            for (Method method : methods) {
                if (!method.getName().equals(name)) {
                    // 首先 名字不同的直接忽略
                    continue;
                }
                Class<?>[] types = method.getParameterTypes();
                if (argTypes != null && types.length == argTypes.length) {
                    for (int i = 0; i < types.length; i++) {
                        if (checkType(types[i], argTypes[i])) {
                            flag = true;
                        } else {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        targetMethod.add(method);
                        flag = false;
                    }
                } else if (argTypes == null) {
                    // 如果传入的参数个数为0 就是没有传入参数
                    // 那么匹配所有同名方法
                    targetMethod.add(method);
                }
            }
            if (targetMethod.size() == 0) {
                throw new NoSuchMethodException();
            } else if (targetMethod.size() > 1) {
                StringBuffer buffer = new StringBuffer("不明确的方法: ");
                for (Method method : targetMethod) {
                    buffer.append(method.getName());
                    buffer.append(",");
                }
                buffer.deleteCharAt(buffer.length() - 1);
                throw new UndeclaredMethodException(buffer.toString());
            } else {
                return targetMethod.get(0);
            }
        }
    }

    public Object execute(BeanSelector handler, String name, Object... args) throws BeanException {
        Class<?> clazz = aliasList.get(name);
        if (clazz == null) {
            throw new BeanException("未知服务: " + name);
        }
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
