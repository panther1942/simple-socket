package cn.erika.util.string;

public class ConsoleUtils {

    /**
     * 终端上画分割线
     *
     * @param character 分割线的字符
     * @param length    分割线的总长度
     */
    public static void drawLine(CharSequence character, int length) {
        drawLine(null, character, length);
    }

    /**
     * 终端上画分割线 带水平居中的标题
     * 当标题字数为奇数个时标题会偏左一个字符
     * 当标题为空时会设置为空字符串
     *
     * @param title     标题
     * @param character 分割线的字符
     * @param length    分割线的总长度（含标题）
     */
    public static void drawLine(String title, CharSequence character, int length) {
        if (title == null) {
            title = "";
        }
        if (length < title.length()) {
            return;
        }

        int i = 0;
        StringBuilder buffer = new StringBuilder();
        for (; i < (length - title.length()) / 2; i++) {
            buffer.append(character);
        }
        buffer.append(title);
        for (; i < length - title.length(); i++) {
            buffer.append(character);
        }
        System.out.println(buffer);
    }
}
