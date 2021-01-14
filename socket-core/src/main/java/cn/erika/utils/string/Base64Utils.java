package cn.erika.utils.string;

/**
 * base64算法 只是个玩具 性能很差 可以拿到jdk6/7上做测试
 * 生成1024位随机字符串 使用Base64编码后解码 循环100k次 速度比jdk8自带的慢将近一倍
 * <p>
 * 编码原理
 * ascii表上的每个字符都能用一个字节表示 也就是8bit
 * Base64表中使用64(+1)个可见字符 只需要6bit
 * 要表示这128个字符 就每三个字节划为一组 共计24bit 划分为4组 每组6bit
 * 即每组表示的字符就在这张表里
 * <p>
 * 如果只有2个字节 则第三个base字节的低2位补0 第四位为'='
 * <p>
 * 如果只有1个字节 则第二个base字节的低4位补0 第三、四位为'='
 * <p>
 * 解码操作反之
 */
public class Base64Utils {
    private static final byte[] base64Map = new byte[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/', '='
    };

    public static byte[] encode(byte[] data) {
        int length = data.length / 3;
        if (data.length % 3 != 0) {
            length++;
        }
        byte[] result = new byte[length * 4];

        int readPos = 0;
        int writePos = 0;

        while (readPos < data.length) {
            byte[] byteArr = new byte[3];
            int[] baseArr = new int[4];
            int arrPos = 0;
            for (; arrPos < 3 && readPos + arrPos < data.length; arrPos++) {
                byteArr[arrPos] = data[readPos + arrPos];
            }
            readPos += arrPos;
            switch (arrPos) {
                case 3:
                    baseArr[0] = (byteArr[0] & 0xFC) >> 2;
                    baseArr[1] = ((byteArr[0] & 0x03) << 4) | ((byteArr[1] & 0xF0) >> 4);
                    baseArr[2] = ((byteArr[1] & 0x0F) << 2) | ((byteArr[2] & 0xC0) >> 6);
                    baseArr[3] = byteArr[2] & 0x3F;
                    break;
                case 2:
                    baseArr[0] = (byteArr[0] & 0xFC) >> 2;
                    baseArr[1] = ((byteArr[0] & 0x03) << 4) | ((byteArr[1] & 0xF0) >> 4);
                    baseArr[2] = (byteArr[1] & 0x0F) << 2;
                    baseArr[3] = 0x40;
                    break;
                case 1:
                    baseArr[0] = (byteArr[0] & 0xFC) >> 2;
                    baseArr[1] = (byteArr[0] & 0x03) << 4;
                    baseArr[2] = 0x40;
                    baseArr[3] = 0x40;
                    break;
            }

            for (int aBaseChar : baseArr) {
                result[writePos++] = base64Map[aBaseChar];
            }
        }
        return result;
    }

    public static byte[] decode(byte[] data) {
        if (data.length % 4 != 0) {
            throw new RuntimeException("不合规的Base64编码");
        }
        byte[] result = new byte[data.length / 4 * 3];

        int writePos = 0;
        int readPos = 0;
        for (int i = 0; i < data.length; i += 4) {
            int[] asciiArr = new int[4];
            int[] byteArr = new int[3];
            for (readPos = 0; readPos < 4 && data[i + readPos] != '='; readPos++) {
                asciiArr[readPos] = getPosByByte(data[i + readPos]);
            }

            switch (readPos) {
                case 4:
                    byteArr[0] = ((asciiArr[0] & 0x3F) << 2) | ((asciiArr[1] & 0x30) >> 4);
                    byteArr[1] = ((asciiArr[1] & 0x0F) << 4) | ((asciiArr[2] & 0x3C) >> 2);
                    byteArr[2] = ((asciiArr[2] & 0x03) << 6) | (asciiArr[3] & 0x3F);
                    break;
                case 3:
                    byteArr[0] = ((asciiArr[0] & 0x3F) << 2) | ((asciiArr[1] & 0x30) >> 4);
                    byteArr[1] = ((asciiArr[1] & 0x0F) << 4) | ((asciiArr[2] & 0x3C) >> 2);
                    break;
                case 2:
                    byteArr[0] = ((asciiArr[0] & 0x3F) << 2) | ((asciiArr[1] & 0x30) >> 4);
                    break;
            }

            for (int j = 0; j < readPos - 1; j++) {
                result[writePos++] = (byte) byteArr[j];
            }
        }
        byte[] tmp = new byte[result.length - 4 + readPos];
        System.arraycopy(result, 0, tmp, 0, tmp.length);
        return tmp;
    }

    private static int getPosByByte(byte b) {
        for (int i = 0; i < base64Map.length; i++) {
            if (base64Map[i] == b) {
                return i;
            }
        }
        return -1;
    }
}