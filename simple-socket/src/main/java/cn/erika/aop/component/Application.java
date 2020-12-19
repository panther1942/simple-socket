package cn.erika.aop.component;

import cn.erika.aop.annotation.Aspect;
import cn.erika.aop.annotation.Component;
import cn.erika.aop.annotation.PackageScan;
import cn.erika.aop.annotation.ServiceMapping;
import cn.erika.aop.exception.BeanException;
import cn.erika.aop.exception.NoSuchBeanException;
import cn.erika.aop.scan.PackageScanner;
import cn.erika.aop.scan.PackageScannerHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 启动类需要继承本方法以实现扫包功能和自动注册服务功能
public abstract class Application {

    // 因为使用懒加载策略 所以需要使用ConcurrentHashMap保证线程安全
    // 存储包含Component的组件
    private static Map<Class<?>, Object> beanList = new ConcurrentHashMap<>();
    // 临时数据存放区
    private static Map<String, Object> storage = new ConcurrentHashMap<>();
    // 服务别名记录 与beanList配合使用
    private static Map<String, Class<?>> aliasList = new HashMap<>();
    // 存储包含ServiceMapping的组件
    private static Map<String, Method> serviceList = new HashMap<>();
    // 排除的bean 将不会主动创建这些对象 但可以手动添加
    private static List<Class<?>> exclusionBean = new LinkedList<>();

    // 需要在启动类手动执行
    // 如果在启动类上有使用PackageScan注解
    // 则将注解上标识的包名进行扫描
    // 如果有自定义处理器需要在本方法执行之前加入到扫包的处理器列表中
    public static void run(Class<? extends Application> clazz, String... args) {
        try {
            Application app = clazz.newInstance();
            app.beforeStartup();
            // 启动前和启动后之间扫包
            app.scanPackage(clazz);
            app.afterStartup();
        } catch (InstantiationException e) {
            throw new RuntimeException("启动类无法实例化", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("启动类需要一个可访问的无参构造函数", e);
        }
    }

    public void run(String... args) {
        Class<? extends Application> clazz = this.getClass();
        beforeStartup();
        // 启动前和启动后之间扫包
        scanPackage(clazz);
        afterStartup();
    }

    // 程序启动前动作 默认添加一个处理器扫描Component和ServiceMapping的注解
    public void beforeStartup() {
        PackageScanner scanner = PackageScanner.getInstance();
        // 默认处理器
        // 查找具有Component注解注释的类 将具有ServiceMapping注解的方法加入服务列表
        scanner.addHandler(new PackageScannerHandler() {
            @Override
            public boolean filter(Class<?> clazz) {
                return clazz.getAnnotation(Component.class) != null;
            }

            @Override
            public void deal(Class<?> clazz) {
                Component component = clazz.getAnnotation(Component.class);
                if (!"".equals(component.value().trim())) {
                    aliasList.put(component.value(), clazz);
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    ServiceMapping mapping = method.getAnnotation(ServiceMapping.class);
                    if (mapping != null) {
                        serviceList.put(mapping.value(), method);
                    }
                }
            }
        });
    }

    private void scanPackage(Class<? extends Application> clazz){
        PackageScan scan = clazz.getAnnotation(PackageScan.class);
        if (scan != null) {
            PackageScanner scanner = PackageScanner.getInstance();
            for (String pack : scan.value()) {
                scanner.addPackage(pack);
            }
            scanner.scan();
        }
    }

    public abstract void afterStartup();

    // 根据服务名称获取服务方法
    public static Object execute(String name, Object... args) throws BeanException {
        Method method = serviceList.get(name);
        Class clazz = method.getDeclaringClass();
        Object object = getBean(clazz);
        try {
            return Proxy.getInvocationHandler(object).invoke(object, method, args);
        } catch (Throwable throwable) {
            throw new BeanException("内部异常", throwable);
        }
    }

    public static void add(String key, Object value) {
        storage.put(key, value);
    }

    public static <T> T get(String key) throws BeanException {
        T target = null;
        if (storage.containsKey(key)) {
            Object obj = storage.get(key);
            try {
                target = (T) obj;
            } catch (ClassCastException e) {
                throw new BeanException(e.getMessage(), e);
            }
        }
        return target;
    }

    public static <T> T pop(String key) throws BeanException {
        T target = get(key);
        storage.remove(key);
        return target;
    }

    public static void addBean(Class<?> clazz, Object obj) {
        beanList.put(clazz, obj);
    }

    // 获取Bean并做好类型转换
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<?> clazz) throws BeanException {
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

    public static <T> T getBean(String name) throws BeanException {
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
    private static <T> T createBean(Class<?> clazz) throws BeanException {
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

    public static boolean existBean(Class<?> clazz) {
        return beanList.containsKey(clazz);
    }

    // 排除列表中的bean不会被创建 只能手动创建并添加到列表中
    public static void excludeBean(Class<?> clazz) {
        exclusionBean.add(clazz);
    }

    // 反射处理器 在这里做了增强Advice
    // 搞成静态内部类是因为只有创建Bean的时候用 而且不想让外部访问
    private static class InvocationProxy implements InvocationHandler {
        // 代理的目标
        private Object target;

        private InvocationProxy(Object target) {
            this.target = target;
        }

        private Method getMethod(String name, Class... argTypes) throws NoSuchMethodException {
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
                        if (argTypes[i] == null) {
                            // 如果传入的参数部分为空 则匹配这个位置任意类型的参数
                            flag = true;
                        } else if (types[i].isAssignableFrom(argTypes[i])) {
                            // 如果参数类型匹配 或为其子类
                            flag = true;
                        } else {
                            // 如果既不为空 且参数类型又不匹配
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
                throw new RuntimeException(buffer.toString());
            } else {
                return targetMethod.get(0);
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = null;
            Advice advice = null;
            // 检查被代理的目标是不是增强器的实现类 如果是的话 跳过AOP检测
            if (!Advice.class.isInstance(target)) {
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
                Aspect aspect = targetMethod.getAnnotation(Aspect.class);
                // 如果存在Aspect注解 则获取增强类的实例
                if (aspect != null) {
                    advice = getBean(aspect.value());
                }
            }
            // 如果增强器不为空 则执行增强部分的方法 否则执行目标方法
            if (advice != null) {
                try {
                    if (advice.before(method, args)) {
                        result = method.invoke(target, args);
                        advice.success(method, args, result);
                        return result;
                    } else {
                        return advice.cancel(method, args);
                    }
                } catch (Throwable e) {
                    return advice.failed(method, args, e);
                } finally {
                    advice.finished(method, args);
                }
            } else {
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
        }
    }
}
