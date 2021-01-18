package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.bean.BeanFactory;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.services.SocketServiceSelector;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.security.SecurityUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseHandler implements Handler {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected BeanFactory beanFactory = BeanFactory.getInstance();
    private List<Task> taskList = new LinkedList<>();
    private ExecutorService servicePool = Executors.newFixedThreadPool(20);

    static {
        try {
            // 初始化的时候生成RSA密钥对 以后可以把密钥对存起来 这玩意一直变也不是个事
            if (GlobalSettings.privateKey == null || GlobalSettings.publicKey == null) {
                byte[][] keyPair = SecurityUtils.initKey();
                System.out.println("初始化密钥");
                GlobalSettings.publicKey = keyPair[0];
                GlobalSettings.privateKey = keyPair[1];
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void init(ISocket socket) {
        // 创建连接或者接入连接的时候初始化参数
        // 刚开始肯定是没有加密的明文传输
        socket.set(Constant.ENCRYPT, false);
        // 认证标识
        socket.set(Constant.AUTHENTICATED, false);
    }

    @Override
    public void onReady(ISocket socket) {
        for (Task task : taskList) {
            task.setSocket(socket);
            servicePool.submit(task);
        }
    }

    @Override
    public void onMessage(ISocket socket, Message message) throws BeanException {
        String serviceName = message.get(Constant.SERVICE_NAME);
        if (serviceName != null) {
            execute(socket, serviceName, message);
        } else {
            log.warn("未知的服务请求: " + message.toString());
        }
    }

    @Override
    public void onError(ISocket socket, Throwable throwable) {
        log.error(throwable.getMessage());
        throwable.printStackTrace();
    }

    protected void execute(ISocket socket, String serviceName, Message message) throws BeanException {
        String type = socket.get(Constant.TYPE);
        if (type != null) {
            servicePool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        beanFactory.execute(new SocketServiceSelector(socket), serviceName, socket, message);
                    } catch (Throwable e) {
                        onError(socket, e);
                    }
                }
            });
        } else {
            throw new BeanException("没有指定服务类型 client或者server");
        }
    }

    public void close() {
        servicePool.shutdown();
    }

    @Override
    public List<Task> getTasks() {
        return this.taskList;
    }

    @Override
    public void addTask(Task task) {
        taskList.add(task);
    }

    protected void addTasks(Collection<Task> tasks) {
        taskList.addAll(tasks);
    }

    @Override
    public void delTask(Task task) {
        taskList.remove(task);
    }

    @Override
    public void emptyTasks() {
        taskList.clear();
    }
}
