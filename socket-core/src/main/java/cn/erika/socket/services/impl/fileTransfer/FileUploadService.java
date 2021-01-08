package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.aop.AuthenticatedCheck;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.context.annotation.Enhance;
import cn.erika.aop.FileUploadTimeCount;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.model.po.FileTransPartRecord;
import cn.erika.socket.model.po.FileTransRecord;
import cn.erika.socket.model.pto.FileInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.socket.model.vo.FileTransInfo;
import cn.erika.socket.orm.IFileTransInfoService;
import cn.erika.socket.orm.IFileTransPartRecordService;
import cn.erika.socket.orm.IFileTransRecordService;
import cn.erika.socket.services.ISocketService;
import cn.erika.utils.exception.UnsupportedAlgorithmException;
import cn.erika.utils.io.FileUtils;
import cn.erika.utils.security.MessageDigestUtils;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.string.Base64Utils;
import cn.erika.utils.string.StringUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Base64;

@Component(Constant.SRV_UPLOAD)
public class FileUploadService extends BaseService implements ISocketService {
    private static DecimalFormat df = new DecimalFormat("0.00%");
    private static int blockSize = 4 * 1024 * 1024;

    private IFileTransInfoService fileTransInfoService;
    private IFileTransPartRecordService fileTransPartRecordService;

    public FileUploadService() throws BeanException {
        this.fileTransInfoService = getBean("fileTransInfoService");
        this.fileTransPartRecordService = getBean("fileTransPartRecordService");
    }

    @Enhance(FileUploadTimeCount.class)
    @Override
    public void client(ISocket socket, Message message) throws IOException {
        // 发送文件仅由发送端自己发起 不接受远端发起
        if (message.get(Constant.SERVICE_NAME) == null) {
            String filename = message.get(Constant.FILENAME);
            String filepath = message.get(Constant.FILEPATH);
            FileInfo info = message.get(Constant.FILE_INFO);
            long skip = info.getPos();
            long partLength = info.getLength();

            File file = new File(filepath);
            try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
                in.seek(skip);
                long pos = 0;
                int len;
                byte[] data = new byte[blockSize];
                log.info("发送文件: " + filename);

                while ((len = in.read(data)) > -1) {
                    // 如果读取的数据累计超过片段长度 则截取至片段长度
                    if (pos + len > partLength) {
                        len = (int) (partLength - pos);
                    }
                    // 复制为新数组 删除掉空白部分
                    byte[] tmp = new byte[len];
                    System.arraycopy(data, 0, tmp, 0, len);
                    Message msg = new Message(Constant.SRV_UPLOAD);
                    // 为了避免序列化和反序列化出现错误 数据使用BASE64编码
                    msg.add(Constant.FILE_POS, pos);
                    msg.add(Constant.BIN, encoder.encodeToString(tmp));
                    msg.add(Constant.LEN, tmp.length);
                    pos += len;
                    log.info("进度: " + df.format(pos / (double) partLength));
                    socket.send(msg);
                    if (pos >= partLength) {
                        break;
                    }
                }
                log.info("发送完成: " + filename);
                Message request = new Message(Constant.SRV_POST_UPLOAD);
                request.add(Constant.SEND_STATUS, "发送完成: " + filename);
                request.add(Constant.UID, info.getUuid());
                socket.send(request);
            }
        }
    }

    @Enhance(AuthenticatedCheck.class)
    @Override
    public void server(ISocket socket, Message message) {
        String token = socket.get(Constant.TOKEN);
        FileInfo info = socket.getHandler().get(token);
        String filename = info.getFilename();
        long partLength = info.getLength();
        long filePos = message.get(Constant.FILE_POS);
        int len = message.get(Constant.LEN);
        String data = message.get(Constant.BIN);

        File file = new File(filename);
        try (RandomAccessFile out = new RandomAccessFile(file, "rwd")) {
            log.info("当前进度: " + df.format((filePos + len) / (double) partLength) + ": " + file.getName());
            out.seek(filePos);
            out.write(decoder.decode(data), 0, len);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            Message error = new Message(Constant.SRV_TEXT);
            error.add(Constant.TEXT, e.getMessage());
            socket.send(error);
            socket.close();
            return;
        }

        try {
            if (filePos + len >= partLength) {
                log.info("传输完成 正在进行数据校验: " + file.getName());
                check(socket, info);
            }
        } catch (IOException e) {
            log.error("校验出错: " + e.getMessage(), e);
        } catch (UnsupportedAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void check(ISocket socket, FileInfo fileInfo) throws IOException, UnsupportedAlgorithmException {
        File file = new File(fileInfo.getFilename());
        String filepath = file.getAbsolutePath();
        log.info("文件位置: " + filepath);
        BaseSocket parent = socket.get(Constant.PARENT_SOCKET);
        long crc = MessageDigestUtils.crc32Sum(file);
        if (fileInfo.getCrc() == crc) {
            log.info("数据完整: " + filepath);
            parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收完成: " + filepath));
            FileTransPartRecord filePart = fileTransPartRecordService.getByUuid(fileInfo.getUuid());
            filePart.setStatus(1);
            filePart.update();
            fileTransInfoService.mergeFile(filePart.getTaskId());
        } else {
            log.warn("数据不完整: " + filepath);
            parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收失败: " + filepath));
        }
    }
}
