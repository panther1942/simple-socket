package cn.erika.config;

import cn.erika.utils.log.LogLevel;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.security.MessageDigestAlgorithm;
import cn.erika.utils.security.SecurityAlgorithm;
import cn.erika.utils.security.SecurityUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ConfigReader {
    private static Logger log = LoggerFactory.getLogger(ConfigReader.class);
    private static String configPath = System.getProperty("user.dir") + "/socket.properties";
    private static ResourceBundle config;

    static {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(configPath))) {
            config = new PropertyResourceBundle(in);
            log.info("找到外部配置文件: " + configPath);
        } catch (IOException e) {
            log.warn("未找到外部配置文件: " + configPath);
        }
        if (config == null) {
            log.warn("使用默认配置运行");
        } else {
            readConfig();
        }
    }

    private static void readConfig() {
        Class configClass = GlobalSettings.class;
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isFinal(field.getModifiers())) {
                try {
                    Object obj = config.getObject(field.getName());
                    setValue(field, obj);
                    log.debug("覆盖默认配置: " + field.getName() + ": " + obj);
                } catch (MissingResourceException ignored) {
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        }
    }

    private static void setValue(Field field, Object value) throws Exception {
        Class<?> fieldType = field.getType();
        field.setAccessible(true);
        if (Charset.class.equals(fieldType)) {
            field.set(null, Charset.forName(String.valueOf(value)));
        } else if (SecurityAlgorithm.class.equals(fieldType)) {
            field.set(null, SecurityUtils.getSecurityAlgorithmByValue(String.valueOf(value)));
        } else if (MessageDigestAlgorithm.class.equals(fieldType)) {
            field.set(null, SecurityUtils.getMessageDigestAlgorithmByValue(String.valueOf(value)));
        } else if (LogLevel.class.equals(fieldType)) {
            field.set(null, LogLevel.getByName(String.valueOf(value)));
        } else {
            switch (fieldType.getSimpleName()) {
                case "short":
                case "Short":
                    field.setShort(null, Short.parseShort(String.valueOf(value)));
                    break;
                case "int":
                case "Integer":
                    field.setInt(null, Integer.parseInt(String.valueOf(value)));
                    break;
                case "long":
                case "Long":
                    field.setLong(null, Long.parseLong(String.valueOf(value)));
                    break;
                case "float":
                case "Float":
                    field.setFloat(null, Float.parseFloat(String.valueOf(value)));
                    break;
                case "double":
                case "Double":
                    field.setDouble(null, Double.parseDouble(String.valueOf(value)));
                    break;
                case "boolean":
                case "Boolean":
                    field.setBoolean(null, Boolean.parseBoolean(String.valueOf(value)));
                    break;
                default:
                    field.set(null, value);
            }
        }
    }
}
