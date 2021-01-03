package cn.erika.utils.io.compress.file;

import cn.erika.utils.exception.CompressException;
import cn.erika.utils.io.compress.CompressAlgorithm;

import java.io.File;
import java.io.IOException;

public interface FileCompress extends CompressAlgorithm {
    // 压缩方法
    public void compress(File src, File dest) throws IOException, CompressException;

    // 压缩方法
    public void compressDir(File directory, File file) throws IOException, CompressException;

    // 压缩方法
    public void compressFiles(File[] files, File archiveFile) throws IOException, CompressException;

    // 解压缩方法
    public void decompress(File archiveFile, File directory) throws CompressException;

    public default boolean compare(Object o) {
        if (!(o instanceof FileCompress)) {
            return false;
        }
        FileCompress that = (FileCompress) o;
        if (this.getCode() == that.getCode()) {
            return true;
        }
        if (this.getName().equalsIgnoreCase(that.getName())) {
            return true;
        }
        return false;
    }
}
