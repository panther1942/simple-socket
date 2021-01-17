package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.context.annotation.Inject;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.socket.exception.LimitThreadException;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.bio.FileSender;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.socket.orm.IFileTransInfoService;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.io.FileUtils;
import cn.erika.utils.security.MessageDigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

//    upload /home/erika/Downloads/config.json config123.json
@Component(Constant.SRV_PRE_UPLOAD)
public class FileUploadPreService extends BaseService implements ISocketService {

    @Inject(name = "fileTransInfoService")
    private IFileTransInfoService transInfoService;
    @Inject(name = "fileTransPartRecordService")
    private IFileTransPartRecordService transPartService;

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void client(ISocket socket, Message message) throws AuthenticateException {
        if (message.get(Constant.SERVICE_NAME) == null) {
            preUpload(socket, message, GlobalSettings.fileThreads);
        } else {
            upload(socket, message);
        }
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void server(ISocket socket, Message message) {
        try {
            // 要保存的文件名
            String remoteFile = message.get(Constant.REMOTE_FILE);
            // 文件基本信息
            FileInfo fileInfo = message.get(Constant.FILE_INFO);
            // 签名信息
            String sign = fileInfo.getSign();
            // 签名算法
            String algorithm = fileInfo.getAlgorithm();
            // 当前目录
            String pwd = socket.get(Constant.PWD);
            // 最终文件保存路径
            File file = null;
            if (remoteFile.startsWith(FileUtils.SYS_FILE_SEPARATOR)) {
                file = new File(remoteFile);
            } else {
                file = new File(pwd + FileUtils.SYS_FILE_SEPARATOR + remoteFile);
            }
            if (file.exists() && FileUtils.checkFile(file, fileInfo)) {
                throw new IOException("文件完整 不需要重传");
            }
            // 文件分片信息
            List<FileInfo> fileInfoList = message.get(Constant.FILE_PART_INFO);
            // 并发数量
            Integer threads = message.get(Constant.THREADS);
            if (threads == null || threads < 1) {
                throw new LimitThreadException("错误的线程数");
            }
            if (threads > GlobalSettings.fileThreadsLimit) {
                throw new LimitThreadException("线程数超出服务器限制");
            }
            IServer server = getBean(IServer.class);

            // 从数据库根据签名和签名算法读取记录
            FileTransInfo transInfo = transInfoService.getTransInfoByFilepath(file.getAbsolutePath());
            // 传输分片信息 最终要向客户端回送这个
            List<FileInfo> infoList = null;
            // 如果数据库存在传输记录 而且存在分片信息 则读取已经保存的分片信息
            if (transInfo != null && transInfo.getParts() != null) {
                infoList = transPartService.getFileInfoList(transInfo.getParts());
                // 如果和数据库里面不一致 则更新记录的信息
                if (!transPartService.checkPartInfo(infoList, fileInfoList)) {
                    // 删除存储的相关信息
                    transInfo.getRecord().delete();
                    for (FileTransPartRecord record : transInfo.getParts()) {
                        record.delete();
                    }
                    // 将列表替换为客户端发来的列表
                    infoList = fileInfoList;
                    // 更新最终存储的文件名
                    for (FileInfo info : infoList) {
                        info.setFilename(pwd + info.getFilename());
                    }
                    // 更新文件记录
                    FileTransRecord record = transInfo.getRecord();
                    record.setFilename(remoteFile);
                    record.setFilepath(file.getAbsolutePath());
                    record.setLength(fileInfo.getLength());
                    record.setThreads(threads);
                    record.setSign(sign);
                    record.setAlgorithm(algorithm);
                    if (record.insert() > 0 || record.getUuid() != null) {
                        // 更新文件片段记录 同时给infoList添加uuid
                        transInfo.setParts(transPartService.createPartInfo(infoList, record));
                    } else {
                        throw new IOException("更新文件记录失败");
                    }
                } else {
                    FileTransRecord record = transInfo.getRecord();
                    record.setThreads(threads);
                    record.setSign(sign);
                    record.setAlgorithm(algorithm);
                    if (record.update() < 1) {
                        throw new IOException("更新文件记录失败");
                    }
                }
            } else {
                transInfo = new FileTransInfo();
                // 不存在则需要创建分片信息 并记录数据库
                infoList = fileInfoList;
                // 更新最终存储的文件名
                for (FileInfo info : infoList) {
                    info.setFilename(pwd + info.getFilename());
                }
                // 添加文件记录
                FileTransRecord record = new FileTransRecord();
                record.setFilename(remoteFile);
                record.setFilepath(file.getAbsolutePath());
                record.setLength(fileInfo.getLength());
                record.setSign(fileInfo.getSign());
                record.setAlgorithm(fileInfo.getAlgorithm());
                record.setThreads(threads);
                record.setSender(socket.get(Constant.USERNAME));
                record.setReceiver("SERVER");
                // 插入数据 如果大于0说明插入成功
                if (record.insert() > 0 || record.getUuid() != null) {
                    transInfo.setRecord(record);
                    // 更新文件片段记录 同时给infoList添加uuid
                    transInfo.setParts(transPartService.createPartInfo(infoList, record));
                } else {
                    throw new IOException("新增文件记录失败");
                }
            }
            // 检查已经传传输的文件
            int flag = 0;
            for (FileInfo info : infoList) {
                File partFile = new File(info.getFilename());
                // 如果文件存在则进行crc32校验
                if (partFile.exists()) {
                    long crc = MessageDigestUtils.crc32Sum(partFile);
                    // 校验错误则删除不完整的文件
                    if (crc != info.getCrc()) {
                        if (!partFile.delete()) {
                            throw new IOException("无法删除未完成的文件: " + partFile.getAbsolutePath());
                        }
                    } else {
                        // 如果完整就设置状态位=1 表示片段文件完整
                        flag++;
                        info.setStatus(1);
                        FileTransPartRecord part = transPartService.getByUuid(info.getUuid());
                        part.setStatus(1);
                        part.update();
                    }
                }
                log.info("准备接收文件: " + info.getFilename());
                // 并设置片段文件的token用于新连接授权
                String partToken = UUID.randomUUID().toString();
                server.addToken(socket, partToken);
                socket.getHandler().add(partToken, info);
                info.setPartToken(partToken);
            }
            if (flag == threads) {
                transInfoService.mergeFile(transInfo.getRecord().getUuid());
            } else {
                FileTransRecord record = transInfo.getRecord();
                record.setStatus(0);
                record.update();
            }
            message.add(Constant.FILE_PART_INFO, infoList);
            message.add(Constant.RECEIVE_STATUS, true);
            socket.send(message);
        } catch (LimitThreadException e) {
            // 如果是线程数量错误 则将服务器设置的线程数返回给客户端
            // 让客户端使用服务器配置重发请求（分段文件的CRC只能是在发送端进行校验）
            log.warn(e.getMessage());
            message.add(Constant.THREADS, GlobalSettings.fileThreads);
            message.add(Constant.RECEIVE_STATUS, false);
            message.add(Constant.TEXT, e.getMessage());
            socket.send(message);
        } catch (Exception e) {
            log.warn(e.getMessage());
            message.del(Constant.THREADS);
            message.add(Constant.RECEIVE_STATUS, false);
            message.add(Constant.TEXT, e.getMessage());
            socket.send(message);
        }
    }

    private void preUpload(ISocket socket, Message message, int threads) {
        String localFile = message.get(Constant.LOCAL_FILE);
        String remoteFile = message.get(Constant.REMOTE_FILE);
        File file = new File(localFile);
        try {
            // 获取文件的文件名（不带路径），文件长度，文件签名和算法
            log.info("计算文件信息: " + localFile);
            FileInfo info = FileUtils.getFileInfo(file);
            Message request = new Message(Constant.SRV_PRE_UPLOAD);
            // 这里的文件名是最终保存的文件名
            request.add(Constant.REMOTE_FILE, remoteFile);
            // 文件的基本信息
            request.add(Constant.FILE_INFO, info);
            // 文件的分片信息
            request.add(Constant.FILE_PART_INFO, transPartService.getFileInfoList(file, remoteFile, threads));
            // 文件的令牌 代替文件的绝对路径
            String fileToken = UUID.randomUUID().toString();
            socket.set(fileToken, localFile);
            request.add(Constant.TOKEN, fileToken);
            // 并发线程数
            request.add(Constant.THREADS, threads);
            log.info("请求发送文件: " + remoteFile);
            socket.send(request);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void upload(ISocket socket, Message message) throws AuthenticateException {
        Boolean status = message.get(Constant.RECEIVE_STATUS);
        String fileToken = message.get(Constant.TOKEN);
        if (fileToken == null) {
            throw new AuthenticateException("缺少文件令牌信息");
        }
        String localFile = socket.get(fileToken);
        try {
            if (status != null && status) {
                List<FileInfo> infoList = message.get(Constant.FILE_PART_INFO);

                boolean flag = false;
                for (FileInfo info : infoList) {
                    if (info.getStatus() == 0) {
                        flag = true;
                        new FileSender(socket, localFile, info.getFilename(), info);
                    }
                }
                if (!flag) {
                    log.info("文件完整 无需传输");
                }
            } else {
                String msg = message.get(Constant.TEXT);
                Integer threads = message.get(Constant.THREADS);
                if (threads != null) {
                    log.warn("使用服务器建议的线程数重试");
                    message.add(Constant.LOCAL_FILE, localFile);
                    preUpload(socket, message, threads);
                } else {
                    socket.remove(fileToken);
                    log.error(msg);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
