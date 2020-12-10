package cn.erika.test;

import cn.erika.socket.common.component.Message;
import cn.erika.socket.common.component.Processor;
import cn.erika.util.compress.CompressException;
import cn.erika.util.compress.GZIP;
import cn.erika.util.string.SerialUtils;
import org.junit.Test;

import java.io.IOException;

public class CoreTest {

    @Test
    public void testInfo() throws CompressException {
        String msg = "hello world";
        byte[] encode = GZIP.compress(msg);

        byte[] decode = GZIP.uncompress(encode);
        System.out.println(new String(decode));
    }

    @Test
    public void testSerial() throws IOException {
        Message message = new Message("Hello");
        message.add("message", "World");
        byte[] data = SerialUtils.serialObject(message);
        Message msg = SerialUtils.serialObject(data);
        System.out.println(msg.toString());
    }
}
