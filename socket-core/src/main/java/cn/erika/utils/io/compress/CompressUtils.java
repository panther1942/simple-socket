package cn.erika.utils.io.compress;

import cn.erika.utils.exception.CompressException;
import cn.erika.utils.exception.NoSuchCompressAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class CompressUtils {
    private static Map<String, CompressAlgorithm> compressAlgorithmMap = new HashMap<>();

    public static void register(CompressAlgorithm algorithm) {
        if (!compressAlgorithmMap.containsKey(algorithm.getName())) {
            compressAlgorithmMap.put(algorithm.getName(), algorithm);
        }
    }

    public static CompressAlgorithm getByName(String algorithm) {
        return compressAlgorithmMap.get(algorithm);
    }

    public static CompressAlgorithm getByCode(int code) {
        for (String name : compressAlgorithmMap.keySet()) {
            CompressAlgorithm algorithm = getByName(name);
            if (algorithm.getCode() == code) {
                return algorithm;
            }
        }
        return null;
    }

    public static byte[] compress(byte[] data, String algorithmName) throws CompressException, NoSuchCompressAlgorithm {
        CompressAlgorithm algorithm = getByName(algorithmName);
        if (algorithm != null) {
            return algorithm.compress(data);
        } else {
            throw new NoSuchCompressAlgorithm("不支持的压缩算法: " + algorithmName);
        }
    }

    public static byte[] decompress(byte[] data, String algorithmName) throws CompressException, NoSuchCompressAlgorithm {
        CompressAlgorithm algorithm = getByName(algorithmName);
        if (algorithm != null) {
            return algorithm.decompress(data);
        } else {
            throw new NoSuchCompressAlgorithm("不支持的压缩算法: " + algorithmName);
        }
    }

    public static byte[] compress(byte[] data, int algorithmCode) throws CompressException, NoSuchCompressAlgorithm {
        CompressAlgorithm algorithm = getByCode(algorithmCode);
        if (algorithm != null) {
            return algorithm.compress(data);
        } else {
            throw new NoSuchCompressAlgorithm("不支持的压缩算法: " + algorithmCode);
        }
    }

    public static byte[] decompress(byte[] data, int algorithmCode) throws CompressException, NoSuchCompressAlgorithm {
        CompressAlgorithm algorithm = getByCode(algorithmCode);
        if (algorithm != null) {
            return algorithm.decompress(data);
        } else {
            throw new NoSuchCompressAlgorithm("不支持的压缩算法: " + algorithmCode);
        }
    }

    public static boolean compare(CompressAlgorithm a1, CompressAlgorithm a2) {
        return a1.getCode() == a2.getCode();
    }

}
