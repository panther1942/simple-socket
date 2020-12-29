package cn.erika.util.string;

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
        int resultPos = 0;
        while (readPos < data.length) {
            byte[] arr = new byte[3];
            int[] baseChar = new int[4];
            int arrPos = 0;
            for (; arrPos < 3 && readPos + arrPos < data.length; arrPos++) {
                arr[arrPos] = data[readPos + arrPos];
            }
            readPos += arrPos;

            switch (arrPos) {
                case 3:
                    baseChar[0] = arr[0] >> 2;
                    baseChar[1] = ((arr[0] & 3) << 4) + (arr[1] >> 4);
                    baseChar[2] = ((arr[1] & 15) << 2) + (arr[2] >> 6);
                    baseChar[3] = arr[2] & 63;
                    break;
                case 2:
                    baseChar[0] = arr[0] >> 2;
                    baseChar[1] = ((arr[0] & 3) << 4) + (arr[1] >> 4);
                    baseChar[2] = (arr[1] & 15) << 2;
                    baseChar[3] = 64;
                    break;
                case 1:
                    baseChar[0] = arr[0] >> 2;
                    baseChar[1] = (arr[0] & 3) << 4;
                    baseChar[2] = 64;
                    baseChar[3] = 64;
                    break;
            }

            for (int aBaseChar : baseChar) {
                result[resultPos++] = base64Map[aBaseChar];
            }
        }
        return result;
    }

    public static byte[] decode(byte[] data) {
        if (data.length % 4 != 0) {
            throw new RuntimeException("不合规的Base64编码");
        }
        byte[] result = new byte[data.length / 4 * 3];
        int resultPos = 0;

        for (int i = 0; i < data.length; i += 4) {
            int[] arr = new int[4];
            int[] baseChar = new int[3];
            int arrPos = 0;
            for (; arrPos < 4 && data[i + arrPos] != '='; arrPos++) {
                arr[arrPos] = getPosByByte(data[i + arrPos]);
            }

            switch (arrPos) {
                case 4:
                    baseChar[0] = ((arr[0] & 63) << 2) + (arr[1] >> 4);
                    baseChar[1] = ((arr[1] & 31) << 4) + (arr[2] >> 2);
                    baseChar[2] = ((arr[2] & 3) << 6) + arr[3];
                    break;
                case 3:
                    baseChar[0] = ((arr[0] & 63) << 2) + (arr[1] >> 4);
                    baseChar[1] = ((arr[1] & 31) << 4) + (arr[2] >> 2);
                    baseChar[2] = (arr[2] & 3) << 6;
                    break;
                case 2:
                    baseChar[0] = ((arr[0] & 63) << 2) + (arr[1] >> 4);
                    baseChar[1] = (arr[1] & 31) << 4;
                    break;
            }

            for (int j = 0; j < arrPos - 1; j++) {
                result[resultPos++] = (byte) baseChar[j];
            }
        }
        return result;
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