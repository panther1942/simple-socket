package cn.erika.socket.services.impl.base;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.Application;
import cn.erika.context.BaseService;
import cn.erika.context.annotation.Component;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.FileInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.services.SocketService;

import java.io.*;
import java.text.DecimalFormat;

@Component(Constant.SRV_UPLOAD)
public class FileUploadService extends BaseService implements SocketService {
    private static DecimalFormat df = new DecimalFormat("0.00%");
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    public void client(Socket socket, Message message) {
        FileInfo info = message.get(Constant.FILE_INFO);
        String filepath = Application.get(socket.get(Constant.TOKEN));
        long skip = info.getFilePos();
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
                msg.add(Constant.BIN, tmp);
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
    public void server(Socket socket, Message message) {
        String token = socket.get(Constant.TOKEN);
        FileInfo info = Application.get(token);
        String filename = info.getFilename();
        long fileLength = info.getFileLength();
        long filePos = message.get(Constant.FILE_POS);
        byte[] data = message.get(Constant.BIN);
        File file = new File(BASE_DIR + filename);

        try (RandomAccessFile out = new RandomAccessFile(file, "rw")) {
            log.info("当前进度: " + df.format((filePos + data.length) / (double) fileLength));
            out.seek(filePos);
            out.write(data, 0, data.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
