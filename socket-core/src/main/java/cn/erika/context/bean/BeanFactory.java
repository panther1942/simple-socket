package cn.erika.context.bean;

import cn.erika.config.Constant;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.context.annotation.Inject;
import cn.erika.context.exception.BeanException;
import cn.erika.context.exception.NoSuchBeanException;
import cn.erika.context.exception.UndeclaredBeanException;
import cn.erika.context.exception.UndeclaredMethodException;
import cn.erika.socket.core.Task;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.string.StringUtils;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器 用于存储、创建、获取实例对象
 */
public class BeanFactory {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    // 单例工厂
    private static BeanFactory factory = new BeanFactory();
    // 存储单例对象
    private Map<Class<?>, Object> beanList = new ConcurrentHashMap<>();
    // 存储服务别名 粒度为Class
    private Map<String, Class<?>> aliasList = new HashMap<>();
    // 存储服务别名 粒度为Method
    private Map<String, Method> serviceList = new HashMap<>();
    // 任务列表 这里仅作记录 客户端和服务器创建的时候会获取
    // 在连接执行到onReady()方法时会执行这些方法
    private List<Task> serverTasks = new LinkedList<>();
    private List<Task> clientTasks = new LinkedList<>();

    private BeanFactory() {
    }

    public static BeanFactory getInstance() {
        if (factory == null) {
            factory = new BeanFactory();
        }
        return factory;
    }

    /**
     * 根据指定的连接类型获取任务列表
     *
     * @param type 连接类型 Constant.CLIENT 客户端 Constant.SERVER 服务器
     * @return 任务列表
     */
    public List<Task> getTasks(String type) {
        switch (type) {
            case Constant.CLIENT:
                return clientTasks;
            case Constant.SERVER:
                return serverTasks;
            default:
                List<Task> taskList = new LinkedList<>();
                taskList.addAll(clientTasks);
                taskList.addAll(serverTasks);
                return taskList;
        }
    }

    /**
     * 为指定的连接类型添加任务列表
     *
     * @param type 连接类型 Constant.CLIENT 客户端 Constant.SERVER 服务器
     * @param task 任务列表
     * @throws BeanException 如果不是以上两种连接类型 将抛出该异常 目前仅支持CS的两种类型
     */
    public void addTasks(String type, Task task) throws BeanException {
        switch (type) {
            case Constant.CLIENT:
                clientTasks.add(task);
                break;
            case Constant.SERVER:
                serverTasks.add(task);
                break;
            default:
                throw new BeanException("不支持的任务类型: " + type);
        }
    }

    /**
     * 添加实例化记录 用于手动实例化对象后添加进容器以管理
     *
     * @param clazz  记录的实例化对象的类名(实际上并没有强关联)
     * @param object 记录的实例化对象
     */
    public void addBean(Class<?> clazz, Object object) {
        Object bean = beanList.get(clazz);
        if (bean != null) {
            log.warn("警告: 覆盖Bean[" + object.getClass().getName() + ":" + object.toString() + "]");
        }
        beanList.put(clazz, object);
    }

    /**
     * 添加实例化记录(粒度为类) 记录其服务名称和服务类
     *
     * @param name  服务名称
     * @param clazz 服务类
     */
    public void addBean(String name, Class<?> clazz) {
        Class beanClass = aliasList.get(name);
        if (beanClass != null) {
            log.warn("警告: 覆盖Bean[" + beanClass.getName() + "]");
        }
        aliasList.put(name, clazz);
    }

    /**
     * 添加实例化记录(粒度为方法) 记录其服务名称和方法
     *
     * @param name   服务名称
     * @param method 服务方法
     */
    public void addBean(String name, Method method) {
        Method beanMethod = serviceList.get(name);
        if (beanMethod != null) {
            log.warn("警告: 覆盖Bean[" + beanMethod.getName() + "]");
        }
        serviceList.put(name, method);
    }

    /**
     * 用于获取实现或者继承某类的实例化对象
     *
     * @param clazz 类名
     * @param <T>   目标类实现或者继承的类/接口
     * @return 包含服务名称和实例化对象的Map对象
     * @throws BeanException 如果在创建实例化对象的时候出现错误
     */
    public <T> Map<String, T> getBeans(Class<T> clazz) throws BeanException {
        Map<String, T> beanList = new HashMap<>();
        for (String name : aliasList.keySet()) {
            if (clazz.isAssignableFrom(aliasList.get(name))) {
                beanList.put(name, getBean(aliasList.get(name)));
            }
        }
        return beanList;
    }

    /**
     * 通过类名获取实例对象
     *
     * @param clazz  实例对象的类
     * @param <T>    目标对象实现或者继承的类/接口
     * @param params 实例化对象需要的参数(可选)
     * @return 如果已缓存则返回缓存中存储的实例化对象 否则新建实例化对象
     * @throws BeanException 如果实例化对象的过程出现错误 如果类型转换出错也将抛出该异常
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<?> clazz, Object... params) throws BeanException {
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
            // 如果没有缓存对象 则尝试创建对象 需要可访问的构造函数
            Component component = clazz.getAnnotation(Component.class);
            if (component != null && !component.ignore()) {
                // 如果clazz具有Component注解 而且不被忽略 则创建对象 否则忽略返回NULL
                bean = createBean(clazz, params);
                // 如果是单例 则缓存对象
                if (Component.Type.SingleTon.equals(component.type())) {
                    beanList.put(clazz, bean);
                }
            }
            return bean;
        }
    }

    /**
     * 通过服务名称获取服务类对象
     * 服务名称支持简写 例如listen服务在没有歧义的情况下可以输入l或者lis即可调用
     *
     * @param name   服务名称
     * @param <T>    目标对象实现或者继承的类/接口
     * @param params 实例化对象需要的参数(可选)
     * @return 如果已缓存则返回缓存中存储的实例化对象 否则新建实例化对象
     * @throws BeanException           如果实例化对象的过程出现错误
     * @throws UndeclaredBeanException 如果服务名称不唯一 则抛出该异常
     */
    public <T> T getBean(String name, Object... params) throws BeanException {
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
            return getBean(aliasList.get(target.get(0)), params);
        }
    }

    /**
     * 用于实例化对象
     *
     * @param clazz 需要实例化对象的类
     * @param args  实例化对象需要的参数
     * @param <T>   实例化对象的接口
     * @return 实例化的对象
     * @throws BeanException 如果构造函数无法访问或者构造方法抛出异常将抛出该异常
     */
    private <T> T createBean(Class<?> clazz, Object... args) throws BeanException {
        try {
            Object obj;
            // 如果参数为空 则尝试调用无参的构造函数
            if (args != null && args.length > 0) {
                Class<?>[] paramTypes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    paramTypes[i] = args[i].getClass();
                }
                // 获取构造方法 如果根据参数获取不到 则尝试调用无参的构造方法
                Constructor<?> con = checkConstructor(clazz, paramTypes);
                if (con != null) {
                    obj = con.newInstance(args);
                } else {
                    throw new NoSuchMethodException();
                }
            } else {
                obj = clazz.newInstance();
            }
            // 实例化完成后将尝试注入属性
            // 只有属性上使用Inject标记才会注入
            for (Field field : clazz.getDeclaredFields()) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject == null) {
                    continue;
                }
                Object fieldBean = null;
                // TODO 注意！！！ 这里可能发生循环引用导致程序初始化失败
                if (inject.clazz() != Void.class) {
                    fieldBean = getBean(inject.clazz());
                } else if (!StringUtils.isEmpty(inject.name())) {
                    fieldBean = getBean(inject.name());
                }
                if (fieldBean != null) {
                    field.setAccessible(true);
                    field.set(obj, fieldBean);
                }
            }
            return getProxy(obj);
        } catch (NoSuchMethodException | InstantiationException e) {
            throw new BeanException("不存在这样的构造函数: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new BeanException("构造函数无法访问: " + e.getMessage());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new BeanException("底层构造方法抛出异常: " + e.getMessage());
        }
    }

    /**
     * 用于获取构造方法
     *
     * @param clazz      要获取构造方法的类
     * @param paramTypes 构造方法使用的参数
     * @return 指定类的构造方法
     */
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

    /**
     * 根据名称和参数从一堆方法里获取指定的方法
     *
     * @param methods    一堆方法 比如object.getClass().getMethods()
     * @param name       方法名称
     * @param paramTypes 方法参数
     * @return 符合名称和参数数量及类型的方法
     * @throws NoSuchMethodException     如果找不到指定的方法
     * @throws UndeclaredMethodException 如果存在多个符合名称和参数的方法
     */
    private Method getMethod(Method[] methods, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        List<Method> targetMethod = new LinkedList<>();
        boolean flag = false;
        for (Method method : methods) {
            if (!method.getName().equals(name)) {
                // 首先 名字不同的直接忽略
                continue;
            }
            Class<?>[] types = method.getParameterTypes();
            if (paramTypes != null && types.length == paramTypes.length) {
                for (int i = 0; i < types.length; i++) {
                    if (checkType(types[i], paramTypes[i])) {
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
            } else if (paramTypes == null && types.length == 0) {
                return method;
            } else if (paramTypes == null) {
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
                buffer.append(method.getName()).append(",");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            throw new UndeclaredMethodException(buffer.toString());
        } else {
            return targetMethod.get(0);
        }
    }

    /**
     * 用于检查参数是否兼容
     *
     * @param src  目标方法的参数
     * @param dest 提供的参数
     * @return 兼容则返回true 否则返回false
     */
    private boolean checkType(Class<?> src, Class<?> dest) {
        if (dest == null) {
            // 如果传入的参数部分为空 则匹配这个位置任意类型的参数
            return true;
        } else if (src.isAssignableFrom(dest)) {
            // 如果参数类型匹配 或为其子类
            return true;
        } else if (src.isPrimitive()) {
            // 最操蛋的就是基本数据类型
            // 不能用isAssignableFrom方法 需要一个一个判断
            String srcName;
            switch (src.getName()) {
                case "byte":
                    srcName = Byte.class.getName();
                    break;
                case "short":
                    srcName = Short.class.getName();
                    break;
                case "int":
                    srcName = Integer.class.getName();
                    break;
                case "long":
                    srcName = Long.class.getName();
                    break;
                case "float":
                    srcName = Float.class.getName();
                    break;
                case "double":
                    srcName = Double.class.getName();
                    break;
                case "boolean":
                    srcName = Boolean.class.getName();
                    break;
                case "char":
                    srcName = Character.class.getName();
                    break;
                default:
                    return false;
            }
            return srcName.equals(dest.getName());
        } else {
            // 如果既不为空 且参数类型又不匹配
            return false;
        }
    }

    /**
     * 获取实例对象的代理对象以实现其增强 AOP部分
     *
     * @param object 实例对象
     * @param <T>    该对象实现的接口
     * @return 该对象的代理对象
     * @throws BeanException 如果代理类没有实现接口 则抛出该异常
     */
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

    /**
     * 反射处理器 AOP
     * 说实话并不喜欢搞内部类 不过只有这里使用 而且没几行就在这写了
     */
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
                // 获取所有形参的类型 下面要根据形参获取实际要执行的方法
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
                Method[] methods = target.getClass().getMethods();
                // 获取目标方法
                Method targetMethod = getMethod(methods, method.getName(), typeArray);
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
    }

    /**
     * 执行服务列表中的方法
     *
     * @param name 服务名称
     * @param args 参数
     * @return 执行结果(如果有)
     * @throws Throwable 如果执行的方法出错
     */
    public Object execute(String name, Object... args) throws Throwable {
        Method method = serviceList.get(name);
        Class<?> clazz = method.getDeclaringClass();
        Object object = getBean(clazz);
        return Proxy.getInvocationHandler(object).invoke(object, method, args);
    }

    /**
     * 执行某个类实例的某个方法
     *
     * @param handler 选择器 用于选择具体执行那种方法
     * @param name    服务名称
     * @param args    参数
     * @return 执行结果(如果有)
     * @throws Throwable 如果执行的方法出错
     */
    public Object execute(BeanSelector handler, String name, Object... args) throws Throwable {
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
        return Proxy.getInvocationHandler(object).invoke(object, method, args);
    }
}
