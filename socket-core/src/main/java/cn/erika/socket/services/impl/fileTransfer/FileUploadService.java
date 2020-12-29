package cn.erika.socket.services.impl.fileTransfer;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.Application;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.FileInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.ISocketService;
import cn.erika.util.security.MessageDigest;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Base64;

@Component(Constant.SRV_UPLOAD)
public class FileUploadService extends BaseService implements ISocketService {
    private static DecimalFormat df = new DecimalFormat("0.00%");
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    public void client(ISocket socket, Message message) {
        FileInfo info = message.get(Constant.FILE_INFO);
        String filepath = Application.get(socket.get(Constant.TOKEN));
        long skip = info.getFilePos();
        File file = new File(filepath);

        try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
            in.seek(skip);
            long pos = skip;
            int len;
            byte[] data = new byte[4 * 1024 * 1024];
            log.info("发送文件: " + file.getAbsolutePath());

            while ((len = in.read(data)) > -1) {
                byte[] tmp = new byte[len];
                System.arraycopy(data, 0, tmp, 0, len);
                Message msg = new Message(Constant.SRV_UPLOAD);
                msg.add(Constant.FILE_POS, pos);
                msg.add(Constant.BIN, Base64.getEncoder().encodeToString(tmp));
                msg.add(Constant.LEN, tmp.length);
                pos += len;
//                log.debug("本次发送长度: " + len);
                log.info("进度: " + df.format(pos / (double) file.length()));
                socket.send(msg);
            }
            log.info("发送完成");
            Message request = new Message(Constant.SRV_POST_UPLOAD);
            request.add(Constant.SEND_STATUS, "发送完成");
            socket.send(request);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void server(ISocket socket, Message message) {
        String token = socket.get(Constant.TOKEN);
        FileInfo info = Application.get(token);
        String filename = info.getFilename();
        long fileLength = info.getFileLength();
        long filePos = message.get(Constant.FILE_POS);
        int len = message.get(Constant.LEN);
        String data = message.get(Constant.BIN);

        File file = new File(BASE_DIR + filename);

        try (RandomAccessFile out = new RandomAccessFile(file, "rwd")) {
            log.info("当前进度: " + df.format((filePos + len) / (double) fileLength));
            out.seek(filePos);
            out.write(Base64.getDecoder().decode(data), 0, len);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (filePos + len >= fileLength) {
            log.info("传输完成 正在进行数据校验");
            try {
                long checkCode = info.getCheckCode();
                log.info("文件位置: " + file.getAbsolutePath());
                BaseSocket parent = socket.get(Constant.PARENT_SOCKET);
                long targetCode = MessageDigest.crc32Sum(file);
                System.out.println("文件校验码: " + targetCode);
                if (checkCode == targetCode) {
                    log.info("数据完整");
                    parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收完成"));
                } else {
                    log.warn("数据不完整");
                    parent.send(new Message(Constant.SRV_POST_UPLOAD, "接收失败"));
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
