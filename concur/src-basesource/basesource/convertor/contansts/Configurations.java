package basesource.convertor.contansts;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置
 * Created by Jake on 2015/6/6.
 */
public class Configurations {

    /**
     * 实例
     */
    private static final Configurations instance = new Configurations();

    /**
     * 配置的Map
     */
    private final Map<String, Object> configures = new HashMap<String, Object>();

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
     * 获取实例
     * @return Configurations
     */
    public static Configurations getInstance() {
        return instance;
    }
}
