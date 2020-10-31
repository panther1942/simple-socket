package cn.erika.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static Random random = new Random();

    public static String randomString(int len) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int idx = random.nextInt(95) + 33;
            buffer.append(String.valueOf((char) idx));
        }
        return buffer.toString();
    }

    public static String[] getParam(String line) {
        List<String> list = new ArrayList<>();
        String regex = "(\"[[^\"].]+\"|\'[[^\'].]+\'|[\\S]+)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(line);
        while (m.find()) {
            list.add(m.group(1).replaceAll("\"", "").replaceAll("\'", ""));
        }
        return list.toArray(new String[list.size()]);
    }

    public static boolean isEmpty(String target){
        return target == null || "".equals(target.trim());
    }
}
