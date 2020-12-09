package cn.erika.socket.handler;

import cn.erika.aop.exception.BeanException;

// cli或者web程序调用的服务全部放在这里 让handler去实现 这样管理方便
// 计划添加NIO的实现类 好麻烦 让我再写几个NIO的demo熟悉一下
// 因为一个客户端接入就要开一个线程 这样并发肯定上不去 读写效率也上不去 系统开销还大
// 所以NIO是必须要走的方向
// BIO是实现简单 而且写了不少demo了 暂时保留 也许会被取代
//
// 通信安全上也许还需要个基于时间的令牌认证
// 现在是通信开始由客户端提供的随机字符串作为AES密钥 并在整个会话期间使用不变更
// 令牌基于时间和客户端ID/会话ID和加密协商的一个随机字符串生成(SHA384之类的散列码)
// 生成令牌将作为AES密钥 且一次性使用 每次发送消息都会生成新的令牌
// 根据通信中的时间戳、客户端ID/会话ID、以及协商的随机字符串可以获取到相同的令牌进行解密操作
// 当然随机字符串的安全性由RSA保证 建议优先考虑会话ID 不过估计要和随机字符串一块传输 随机字符串不定长度 挺头疼
// 客户端ID在实现上会简单一些 先用客户端ID实现看看吧
//
// 基础通信头以及定下来了 包含{时间戳、压缩方式、偏移量、长度}
// 便宜量是为了以后传输大文件用的 确保分包不会乱
// 更细致的协议我寻思要不要像http那样 包括head和body两部分
// 现在的实现类的Message就是这样弄得 总觉得不爽 因为body里的数据类型不定 现在是string byte[] map
// 总觉得还是定一个比较好 说实话我觉得用json算了 就是解析起来麻烦一点 没有像object那么方便
// 或者就是map的K-V对 这样也方便 你有啥就在key上做命名规范好了
// 好 就定K-V对的map了 反正KEY全部放常量表（Constant类）里
// 困了 睡!
public interface IClient {

    public void connect();

    public void close();

    public void send(String message);

    public void upload(String filepath, String filename) throws BeanException;
}
