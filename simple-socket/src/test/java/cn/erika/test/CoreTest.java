package cn.erika.test;

import cn.erika.util.compress.CompressException;
import cn.erika.util.compress.GZIP;
import org.junit.Test;

public class CoreTest {

    @Test
    public void testInfo() throws CompressException {
        String msg = "hello world";
        byte[] encode = GZIP.compress(msg);

        byte[] decode = GZIP.uncompress(encode);
        System.out.println(new String(decode));
    }
}
