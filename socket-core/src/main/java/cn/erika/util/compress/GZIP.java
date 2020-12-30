package cn.erika.util.compress;

import cn.erika.util.exception.CompressException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIP {

    public static byte[] compress(byte[] data) throws CompressException {
        if (data == null || data.length == 0) {
            return data;
        }
        try {
            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            GZIPOutputStream out = new GZIPOutputStream(writer);
            out.write(data);
            out.flush();
            out.close();
            writer.close();
            return writer.toByteArray();
        } catch (IOException e) {
            throw new CompressException("压缩过程中发生异常", e);
        }
    }

    public static byte[] uncompress(byte[] data) throws CompressException {
        if (data == null || data.length == 0) {
            return data;
        }
        try {
            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            ByteArrayInputStream reader = new ByteArrayInputStream(data);
            GZIPInputStream in = new GZIPInputStream(reader);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) > -1) {
                writer.write(buffer, 0, len);
            }
            in.close();
            reader.close();
            writer.close();
            return writer.toByteArray();
        } catch (IOException e) {
            throw new CompressException("解压过程中发生异常", e);
        }
    }
}
