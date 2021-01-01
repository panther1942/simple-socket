package cn.erika.utils.io.compress;

import cn.erika.utils.exception.CompressException;
import cn.erika.utils.exception.NoSuchCompressAlgorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * 压缩工具类
 */
public class CompressUtils {
    private static Map<Integer, CompressAlgorithm> compressAlgorithmMap = new HashMap<>();

    /**
     * 注册压缩算法
     * 如果已经注册过将覆盖原先注册过的算法
     *
     * @param algorithm 压缩算法
     */
    public static void register(CompressAlgorithm algorithm) {
        compressAlgorithmMap.put(algorithm.getCode(), algorithm);
    }

    /**
     * 通过名称获取压缩算法
     *
     * @param algorithmName 压缩算法名称
     * @return 压缩算法类
     */
    public static CompressAlgorithm getByName(String algorithmName) {
        for (int code : compressAlgorithmMap.keySet()) {
            CompressAlgorithm algorithm = getByCode(code);
            if (algorithm.getName().equalsIgnoreCase(algorithmName)) {
                return algorithm;
            }
        }
        return null;
    }

    /**
     * 通过代号获取压缩算法
     *
     * @param algorithmCode 压缩算法代号
     * @return 压缩算法
     */
    public static CompressAlgorithm getByCode(int algorithmCode) {
        return compressAlgorithmMap.get(algorithmCode);
    }

    /**
     * 压缩数据的方法
     *
     * @param data          要压缩的数据
     * @param algorithmName 算法名称
     * @return 压缩的数据
     * @throws CompressException       如果压缩过程出现错误
     * @throws NoSuchCompressAlgorithm 如果不存在这样的压缩算法
     */
    public static byte[] compress(byte[] data, String algorithmName) throws CompressException, NoSuchCompressAlgorithm {
        CompressAlgorithm algorithm = getByName(algorithmName);
        if (algorithm != null) {
            return algorithm.compress(data);
        } else {
            throw new NoSuchCompressAlgorithm("不支持的压缩算法: " + algorithmName);
        }
    }

    /**
     * 解压缩数据的方法
     *
     * @param data          要解压缩的数据
     * @param algorithmName 算法名称
     * @return 解压缩的数据
     * @throws CompressException       如果解压缩过程出现错误
     * @throws NoSuchCompressAlgorithm 如果不存在这样的压缩算法
     */
    public static byte[] uncompress(byte[] data, String algorithmName) throws CompressException, NoSuchCompressAlgorithm {
        CompressAlgorithm algorithm = getByName(algorithmName);
        if (algorithm != null) {
            return algorithm.uncompress(data);
        } else {
            throw new NoSuchCompressAlgorithm("不支持的压缩算法: " + algorithmName);
        }
    }

    /**
     * 压缩数据的方法
     *
     * @param data          要压缩的数据
     * @param algorithmCode 算法代号
     * @return 压缩的数据
     * @throws CompressException       如果压缩过程出现错误
     * @throws NoSuchCompressAlgorithm 如果不存在这样的压缩算法
     */
    public static byte[] compress(byte[] data, int algorithmCode) throws CompressException, NoSuchCompressAlgorithm {
        CompressAlgorithm algorithm = getByCode(algorithmCode);
        if (algorithm != null) {
            return algorithm.compress(data);
        } else {
            throw new NoSuchCompressAlgorithm("不支持的压缩算法: " + algorithmCode);
        }
    }

    /**
     * 解压缩数据的方法
     *
     * @param data          要解压缩的数据
     * @param algorithmCode 算法代号
     * @return 解压缩的数据
     * @throws CompressException       如果解压缩过程出现错误
     * @throws NoSuchCompressAlgorithm 如果不存在这样的压缩算法
     */
    public static byte[] uncompress(byte[] data, int algorithmCode) throws CompressException, NoSuchCompressAlgorithm {
        CompressAlgorithm algorithm = getByCode(algorithmCode);
        if (algorithm != null) {
            return algorithm.uncompress(data);
        } else {
            throw new NoSuchCompressAlgorithm("不支持的压缩算法: " + algorithmCode);
        }
    }
}
