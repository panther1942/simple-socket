package cn.erika.socket.service.impl;

import cn.erika.aop.annotation.Aspect;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.advice.FileAdvise;
import cn.erika.socket.annotation.SocketServiceMapping;
import cn.erika.socket.component.FileInfo;
import cn.erika.socket.component.Message;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.service.ISocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;

@SocketServiceMapping(Constant.SRV_UPLOAD)
public class FileUploadService implements ISocketService {
    private static DecimalFormat df = new DecimalFormat("0.00%");
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    @Aspect(FileAdvise.class)
    public void client(BaseSocket socket, Message message) {
        FileInfo info = message.get(Constant.FILE_INFO);
        String filepath = info.getFilepath();
        long skip = message.get(Constant.FILE_POS);
        File file = new File(filepath);

        try (FileInputStream in = new FileInputStream(file)) {
            in.skip(skip);
            long pos = skip;
            int len;
            byte[] data = new byte[8 * 1024 * 1024];
            log.info("发送文件: " + file.getAbsolutePath());

            while ((len = in.read(data)) > -1) {
                byte[] tmp = new byte[len];
                System.arraycopy(data, 0, tmp, 0, len);
                Message msg = new Message(Constant.SRV_UPLOAD);
                msg.add(Constant.FILE_POS, pos);
                msg.add(Constant.PAYLOAD, tmp);
                pos += len;
//                log.debug("本次发送长度: " + len);
                log.info("进度: " + df.format(pos / (double) file.length()));
                socket.send(msg);
            }
            log.info("发送完成");
            Message request = new Message(Constant.SRV_POST_UPLOAD);
            request.add(Constant.SEND_STATUS, "发送完成");
            socket.send(request);
            socket.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        try {
            String sessionToken = socket.get(Constant.SESSION_TOKEN);
            FileInfo info = App.get(sessionToken);
            String filename = info.getFilename();
            long fileLength = info.getFileLength();
            long filePos = message.get(Constant.FILE_POS);
            byte[] data = message.get(Constant.PAYLOAD);
            File file = new File(BASE_DIR + filename);

            try (RandomAccessFile out = new RandomAccessFile(file, "rw")) {
//                log.debug("解析完成 写入数据:" + data.length);
                log.info("当前进度: " + df.format((filePos + data.length) / (double) fileLength));
                out.seek(filePos);
                out.write(data, 0, data.length);
//                out.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (BeanException e) {
            e.printStackTrace();
        }
    }
}
