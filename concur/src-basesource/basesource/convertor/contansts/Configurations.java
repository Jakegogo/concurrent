package basesource.convertor.contansts;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置
 * Created by Jake on 2015/6/6.
 */
public class Configurations {

    /** 文件保存名称 */
    private static String SAVE_FILE_NAME = "config.properties";

    /**
     * 实例
     */
    private static final Configurations instance = new Configurations();

    /**
     * 配置的Map
     */
    private final Map<String, Object> configures = new HashMap<String, Object>();


    private Configurations() {

    }

    /**
     * 获取配置
     * @param configKey 配置的KEY
     * @param <T>
     * @return
     */
    public static <T> T getConfigure(String configKey) {
        return (T) getInstance().configures.get(configKey);
    }

    /**
     * 加载配置
     */
    public static void loadConfigure() {
        String dir = System.getProperty("user.dir");
        String path = dir + File.separator + SAVE_FILE_NAME;
        Map<String, Object> configures = getInstance().configures;

        Properties properties = new Properties();
        FileInputStream inStream;
        try {
            inStream = new FileInputStream(path);
            properties.load(inStream);

            configures.clear();

            // 读取常量值
            readContants(configures);

            for (String key : properties.stringPropertyNames()) {
                configures.put(key, properties.getProperty(key));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取常量值
    private static void readContants(Map<String, Object> configures) {
        for (Field field : DefaultUIConstant.class.getDeclaredFields()) {
            try {
                configures.put(field.getName(), field.get(DefaultUIConstant.class));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存配置
     * @param configKey
     * @param value
     */
    public static void saveConfigure(String configKey, Serializable value) {
        Map<String, Object> configures = getInstance().configures;
        configures.put(configKey, value);
        outPutConfigure(configures);
    }

    /**
     * 输出配置到文件
     * @param configures Map<String, Object> 键 - 值
     */
    private static void outPutConfigure(Map<String, Object> configures) {
        // 保存文件
        Properties properties = new Properties();
        for (Map.Entry<String, Object> entry : configures.entrySet()) {
            properties.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }

        String dir = System.getProperty("user.dir");
        String path = dir + File.separator + SAVE_FILE_NAME;
        try {
            FileOutputStream outputFile = new FileOutputStream(path);
            properties.store(outputFile, "用户配置");
            outputFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /**
     * 获取实例
     * @return Configurations Configurations
     */
    public static Configurations getInstance() {
        return instance;
    }
}
