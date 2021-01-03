package cn.erika.utils.io.compress.stream;

import cn.erika.utils.exception.CompressException;
import cn.erika.utils.io.compress.CompressUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIP implements StreamCompress {
    public static final String NAME = "GZIP";
    public static final int CODE = 0x01;

    static {
        CompressUtils.register(new GZIP());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getCode() {
        return CODE;
    }

    @Override
    public byte[] compress(byte[] data) throws CompressException {
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

    @Override
    public byte[] uncompress(byte[] data) throws CompressException {
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

    @Override
    public boolean equals(Object o) {
        return compare(o);
    }
}
