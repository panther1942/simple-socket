package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.LinkManager;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.core.Task;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.socket.exception.TokenException;
import cn.erika.utils.db.JdbcUtils;
import cn.erika.utils.security.MessageDigestUtils;

import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseServer extends BaseHandler implements Runnable {
    private LinkManager linkManager = new LinkManager();
    private Map<String, ISocket> tokenList = new ConcurrentHashMap<>();
    // 运行时数据存放区 可以用redis代替
    private Map<String, Object> storage = new ConcurrentHashMap<>();


    public BaseServer() {
        // 测试数据库连接
        JdbcUtils.getInstance();
        List<Task> taskList = beanFactory.getTasks(Constant.SERVER);
        addTasks(taskList);
    }

    @Override
    public void init(ISocket socket) {
        super.init(socket);
        log.info("新连接接入: " + socket.getRemoteAddress());
        linkManager.addLink(socket);
    }

    @Override
    public void onConnected(ISocket socket) throws BeanException {
        log.info("连接初始化完成: " + socket.get(Constant.UID));
    }

    @Override
    public void onClose(ISocket socket) {
        log.info("客户端离线: " + socket.get(Constant.UID));
        linkManager.popLink(socket);
    }

    public void addToken(ISocket socket, String token) throws AuthenticateException {
        if (!tokenList.containsKey(token)) {
            tokenList.put(token, socket);
        } else {
            throw new TokenException("存在相同的token: " + token);
        }
    }

    public ISocket checkToken(String token, byte[] publicKey) throws AuthenticateException {
        ISocket socket = tokenList.get(token);
        if (socket == null) {
            throw new TokenException("token无效");
        } else {
            byte[] pubKey = socket.get(Constant.PUBLIC_KEY);
            long srcToken = MessageDigestUtils.crc32Sum(pubKey);
            long targetToken = MessageDigestUtils.crc32Sum(publicKey);
            if (srcToken == targetToken) {
                tokenList.remove(token);
                return socket;
            } else {
                throw new TokenException("token不匹配: " + token);
            }
        }
    }

    public void status() {
        StringBuffer buffer = new StringBuffer();
        Date now = new Date();
        for (String id : linkManager.getLinks().keySet()) {
            ISocket socket = linkManager.getLink(id);
            Date linkTime = socket.get(Constant.LINK_TIME);
            SocketAddress address = socket.getRemoteAddress();
            String line = String.format("[%d]id: %s From: %s", (now.getTime() - linkTime.getTime()) / 1000, id, address);
            buffer.append(line).append(System.lineSeparator());
        }
        log.info(buffer.toString());
    }

    public void send(String uid, String message) {
        try {
            ISocket socket = linkManager.getLink(uid);
            if (socket != null) {
                socket.send(new Message(Constant.SRV_TEXT, message));
            } else {
                throw new SocketException("不存在UID: " + uid);
            }
        } catch (SocketException e) {
            log.warn(e.getMessage());
        }
    }

    public void listen() {
        Thread t = new Thread(this, this.getClass().getSimpleName());
        t.setName(this.getClass().getSimpleName());
        t.start();
    }


    public void set(String key, Object value) {
        storage.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) storage.get(key);
    }

    public void remove(String key) {
        storage.remove(key);
    }

}
