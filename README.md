## TCP通讯软件
完全用Java开发 尽量少的依赖第三方库 以后也许会加上UDP（没考虑好UDP做啥）  
目前依赖fastjson AOP我用自己写的就够了  
原本只是做一个文件服务器代替ftp服务器和客户端 方便整合到spring项目里面  
自打没工作之后就沉迷于CLI程序 因为不会做界面（审美问题 太丑了）  

#### 主要功能
1. 文本发送与接收  
2. 通讯加密(RSA+AES/DES/TDES...)  
3. 文件的传输与接收（上传/下载）  
4. 远程目录浏览
5. 按需添加...  

#### 软件使用
1. 控制台程序 将CliApplication作为启动类 打包完后 java -jar 编译的jar文件
2. 不带控制台的程序 打包后作为依赖 调用SocketApplication.run(SocketApplication.class)即可启动服务器

客户端不需要使用数据库 我在想文件下载的断点续传咋搞  
上传可以服务器整数据库 下载估计要把元数据写到一个文件里 慢慢搞吧  

#### 项目结构
- simple-socket作为管理整个项目使用  
- socket-core为核心程序
- socket-spring为web端测试(暂时没做 就做了个ws)

#### socket-core结构
- aop 一些增强方法 需要配合下面的容器使用
- cli 这是控制台程序的入口 解析用户的输入并调用socket服务
- config 公共配置都放在这  
    - Constant 全局常量 用来定义一些固定的名称 比如属性名 服务名
    - GlobalSettings 全局配置类 以后会搞用文件做配置
- context 容器 用来存放单例的服务（bean）以及包扫描 热加载不做了（梦开始的地方）自动注入搞好了
- socket 项目核心
    - core 核心部分
    - exception 定义一些异常好截获
    - handler 服务器和客户端
    - model 模型类 po的实体类 需要结合db的工具类使用
    - orm 持久化层的服务类 需要结合db的工具类使用 （相当于web项目的dao+service层）
    - services Socket的服务类（相当于web项目的controller层）
- utils 工具类
    - io 输入输出相关的工具
        - compress 数据压缩相关的工具类
        - FileUtils 文件操作相关的工具类
        - KeyboardReader 从键盘读取一行字符串 以回车为行尾标识（\n）
        - SerialUtils 序列化工具 包括了jdk的序列化和fastjson的序列化  
        `我就纳闷为啥jdk的序列化网络传输后反序列化会出错`
    - db 小型的半自动数据库工具
    - exception 工具类的异常都放在这里
    - log 日志工具集 目前简单实现了终端日志和文件日志 以后会和jdk的日志对接
    - security 安全加密工具集
        - 不对称加密 RSA（JDK目前就支持RSA 其他的要第三方库）
        - 对称加密 AES DES TDES 支持扩展 需实现cn.erika.utils.security.MessageDigestAlgorithm接口
        - 数字签名 RSA,DSA之类的 支持扩展 需实现cn.erika.utils.security.DigitalSignatureAlgorithm接口
        - 消息摘要 MD5 SHA384之类的 支持扩展cn.erika.utils.security.SecurityAlgorithm接口
    - string 字符串工具集
        - Base64Utils 自己实现了一下BASE64算法 感觉这应该作为大学计算机科的必考题 又不难
            当然 性能很渣 差不多是jdk8内置Base64性能的一半左右
        - ConsoleUtils 控制台输出格式化 提供了标题居中输出 和仿照spring日志格式的输出字符串
        - StringUtils 提供了生成指定长度的字符串和字节数组和十六进制字符串之间的转换
        
