package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.handler.IServer;
import cn.erika.socket.handler.bio.FileSender;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.socket.orm.IFileTransInfoService;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.socket.orm.IFileTransRecordService;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.io.FileUtils;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.security.MessageDigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Component(Constant.SRV_PRE_UPLOAD)
public class FileUploadPreService extends BaseService implements ISocketService {
//    upload /home/erika/Downloads/config.json config123.json

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private IFileTransInfoService transInfoService;
    private IFileTransRecordService fileTransRecordService;
    private IFileTransPartRecordService fileTransPartRecordService;

    public FileUploadPreService() throws BeanException {
        this.transInfoService = getBean("fileTransInfoService");
        this.fileTransRecordService = getBean("fileTransRecordService");
        this.fileTransPartRecordService = getBean("fileTransPartRecordService");
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void client(ISocket socket, Message message) {
        if (message.get(Constant.SERVICE_NAME) == null) {
            preUpload(socket, message, GlobalSettings.threads);
        } else {
            checkUpload(socket, message);
        }
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void server(ISocket socket, Message message) {
        try {
            // 要保存的文件名
            String filename = message.get(Constant.FILENAME);
            // 文件基本信息
            FileInfo fileInfo = message.get(Constant.FILE_INFO);
            // 文件分片信息
            List<FileInfo> filePartInfo = message.get(Constant.FILE_PART_INFO);
            // 并发数量
            Integer threads = message.get(Constant.THREADS);
            if (threads == null || threads < 1) {
                threads = 1;
            }
            if (threads > GlobalSettings.threadsLimit) {
                threads = GlobalSettings.threadsLimit;
            }
            // 签名信息
            String sign = fileInfo.getSign();
            // 签名算法
            String algorithm = fileInfo.getAlgorithm();
            // 当前目录
            String pwd = socket.get(Constant.PWD);
            // 最终文件保存路径
            File file = new File(pwd + FileUtils.SYS_FILE_SEPARATOR + filename);
            IServer server = getBean(IServer.class);

            // 从数据库根据签名和签名算法读取记录
            FileTransInfo transInfo = transInfoService.getTransInfoByFilepath(file.getAbsolutePath());
            // 传输分片信息 最终要向客户端回送这个
            List<FileInfo> infoList = null;
            // 如果数据库存在传输记录 而且存在分片信息 则读取已经保存的分片信息
            if (transInfo != null && transInfo.getParts() != null) {
                infoList = getFileInfoList(transInfo);
                if (!checkPartInfo(infoList, filePartInfo)) {
                    // 如果和数据库里面不一致 则更新记录的信息
                    for (FileTransPartRecord record : transInfo.getParts()) {
                        record.delete();
                    }
                    infoList = filePartInfo;
                    for (FileInfo info : infoList) {
                        info.setFilename(pwd + info.getFilename());
                    }
                    FileTransRecord record = transInfo.getRecord();
                    record.setFilename(filename);
                    record.setFilepath(file.getAbsolutePath());
                    record.setLength(fileInfo.getLength());
                    record.setThreads(threads);
                    record.setSign(sign);
                    record.setAlgorithm(algorithm);
                    record.update();
                    List<FileTransPartRecord> partList = createPartInfo(infoList, record);
                    for (int i = 0; i < partList.size(); i++) {
                        infoList.get(i).setUuid(partList.get(i).getUuid());
                    }
                } else {
                    for (int i = 0; i < transInfo.getParts().size(); i++) {
                        infoList.get(i).setUuid(transInfo.getParts().get(i).getUuid());
                    }
                }
            } else {
                // 不存在则需要创建分片信息 并记录数据库
                infoList = filePartInfo;
                for (FileInfo info : infoList) {
                    info.setFilename(pwd + info.getFilename());
                }
                FileTransRecord record = fileTransRecordService.createTransRecord(
                        filename, file.getAbsolutePath(), threads, fileInfo, socket.get(Constant.USERNAME), "SERVER");
                if (record.insert() > 0) {
                    List<FileTransPartRecord> partList = createPartInfo(infoList, record);
                    for (int i = 0; i < partList.size(); i++) {
                        infoList.get(i).setUuid(partList.get(i).getUuid());
                    }
                } else {
                    log.error("无法写入数据库");
                    throw new IOException("服务器错误");
                }
            }
            for (FileInfo info : infoList) {
                File partFile = new File(info.getFilename());
                if (partFile.exists()) {
                    long crc = MessageDigestUtils.crc32Sum(partFile);
                    if (crc != info.getCrc() && !partFile.delete()) {
                        throw new IOException("无法删除未完成的文件: " + partFile.getAbsolutePath());
                    }
                }
                String partToken = UUID.randomUUID().toString();
                server.addToken(socket, partToken);
                socket.getHandler().add(partToken, info);
                info.setPartToken(partToken);
            }

            message.add(Constant.FILE_PART_INFO, infoList);
            message.add(Constant.RECEIVE_STATUS, true);
            socket.send(message);
        } catch (Exception e) {
            log.warn(e.getMessage());
            message.add(Constant.RECEIVE_STATUS, false);
            message.add(Constant.TEXT, e.getMessage());
            socket.send(message);
        }
    }

    private void preUpload(ISocket socket, Message message, int threads) {
        String filename = message.get(Constant.FILENAME);
        String filepath = message.get(Constant.FILEPATH);
        File file = new File(filepath);
        try {
            if (!file.canRead()) {
                throw new IOException("文件不可读");
            }
            // 获取文件的文件名（不带路径），文件长度，文件签名和算法
            FileInfo info = FileUtils.getFileInfo(file);
            Message request = new Message(Constant.SRV_PRE_UPLOAD);
            // 这里的文件名是最终保存的文件名
            request.add(Constant.FILENAME, filename);
            // 文件的基本信息
            request.add(Constant.FILE_INFO, info);
            // 文件的分片信息
            request.add(Constant.FILE_PART_INFO, getFileInfoList(info, file, threads));
            // 文件的令牌 代替文件的绝对路径
            String fileToken = UUID.randomUUID().toString();
            socket.add(fileToken, filepath);
            request.add(Constant.TOKEN, fileToken);
            // 并发线程数
            request.add(Constant.THREADS, threads);
            socket.send(request);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void checkUpload(ISocket socket, Message message) {
        Boolean status = message.get(Constant.RECEIVE_STATUS);
        try {
            if (status != null && status) {
                String fileToken = message.get(Constant.TOKEN);
                String filepath = socket.get(fileToken);
                List<FileInfo> infoList = message.get(Constant.FILE_PART_INFO);
                for (FileInfo info : infoList) {
                    new FileSender(socket, filepath, info);
                }
            } else {
                Integer threads = message.get(Constant.THREADS);
                if (threads != null) {
                    preUpload(socket, message, threads);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }

    // 读取分片信息
    private List<FileInfo> getFileInfoList(FileTransInfo transInfo) {
        List<FileInfo> fileInfoList = new LinkedList<>();
        List<FileTransPartRecord> parts = transInfo.getParts();
        for (FileTransPartRecord part : parts) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilename(part.getFilename());
            fileInfo.setLength(part.getLength());
            fileInfo.setPos(part.getPos());
            fileInfo.setCrc(part.getCrc());
            fileInfoList.add(fileInfo);
        }
        return fileInfoList;
    }

    // 创建分片信息
    private List<FileInfo> getFileInfoList(FileInfo fileInfo, File file, int threads) throws IOException {
        List<FileInfo> fileInfoList = new LinkedList<>();
        // 分片的保存文件名
        String filename = FileUtils.SYS_FILE_SEPARATOR + fileInfo.getFilename() + Constant.FILE_PART_POSTFIX;
        // 文件长度
        long fileLength = fileInfo.getLength();
        // 分片长度
        long partLength = fileLength / threads;
        // 创建分片信息
        for (int i = 0; i < threads; i++) {
            FileInfo partInfo = new FileInfo();
            // 文件名依次加1
            partInfo.setFilename(filename + i);
            // 分片长度
            partInfo.setLength(partLength);
            // 偏移量
            partInfo.setPos(i * partLength);
            // 最后一个分片的长度等于剩余的全部长度
            if (i == threads - 1) {
                partInfo.setLength(fileLength - partLength * i);
            }
            partInfo.setCrc(MessageDigestUtils.crc32Sum(file, partInfo.getPos(), partInfo.getLength()));
            fileInfoList.add(partInfo);
        }
        return fileInfoList;
    }

    private boolean checkPartInfo(List<FileInfo> src, List<FileInfo> dest) {
        if (src == null || dest == null) {
            return false;
        }
        if (src.size() != dest.size()) {
            return false;
        }
        for (int i = 0; i < src.size(); i++) {
            if (Long.compare(src.get(i).getCrc(), dest.get(i).getCrc()) != 0) {
                return false;
            }
            if (src.get(i).getPos() != dest.get(i).getPos()) {
                return false;
            }
            if (src.get(i).getLength() != dest.get(i).getLength()) {
                return false;
            }
        }
        return true;
    }

    // 不存在则需要创建分片信息 并记录数据库
    private List<FileTransPartRecord> createPartInfo(List<FileInfo> partInfo, FileTransRecord record) {
        List<FileTransPartRecord> parts = new LinkedList<>();
        for (int i = 0; i < partInfo.size(); i++) {
            FileTransPartRecord partRecord = fileTransPartRecordService.addFileTransPartRecord(partInfo.get(i), record);
            parts.add(partRecord);
        }
        return parts;
    }
}
