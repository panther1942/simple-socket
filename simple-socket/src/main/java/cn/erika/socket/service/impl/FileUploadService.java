package cn.erika.socket.service.impl;

import cn.erika.aop.annotation.Component;
import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.socket.service.ISocketService;
import cn.erika.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component(Constant.SRV_UPLOAD)
public class FileUploadService implements ISocketService {
    private static DecimalFormat df = new DecimalFormat("0.00%");
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final String BASE_DIR = GlobalSettings.baseDir;

    @Override
    public void client(BaseSocket socket, Message message) {
        String filepath = message.get(Constant.FILEPATH);
        Object filePos = message.get(Constant.FILE_POS);
        long skip = StringUtils.parseLong(filePos);
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
                log.debug("本次发送长度: " + len);

                Message msg = new Message(Constant.SRV_UPLOAD);
                msg.add(Constant.FILE_POS, pos);
                msg.add(Constant.PAYLOAD, tmp);
                pos += len;
                log.info("进度: " + df.format(pos / (double) file.length()));
                socket.send(msg);
            }
            log.info("发送完成");
            socket.send(new Message(Constant.SRV_POST_UPLOAD, new HashMap<String, Object>() {
                {
                    put(Constant.SEND_STATUS, "发送完成");
                }
            }));
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
            Map<String, Object> record = App.get(sessionToken);
            String filename = (String) record.get(Constant.FILENAME);
            Object fileLength = record.get(Constant.FILE_LENGTH);
            Object filePos = message.get(Constant.FILE_POS);
            long length = StringUtils.parseLong(fileLength);
            long pos = StringUtils.parseLong(filePos);
            String payload = message.get(Constant.PAYLOAD);
            byte[] data = Base64.getDecoder().decode(payload.getBytes(GlobalSettings.charset));

            File file = new File(BASE_DIR + filename);
            try (FileOutputStream out = new FileOutputStream(file, true)) {
                log.debug("解析完成 写入数据:" + data.length);
                log.info("当前进度: " + df.format((pos + data.length) / (double) length));
                out.write(data, 0, data.length);
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
