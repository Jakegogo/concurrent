package basesource.convertor.model;

import java.io.File;

/**
 * 用户配置
 * Created by Jake on 2015/6/2.
 */
public class UserConfig {

    private static final UserConfig instance = new UserConfig();

    private File outputPath;


    public static UserConfig getInstance() {
        return instance;
    }

    public void changeOutPutPath(File outputPath) {
        this.outputPath = outputPath;
    }

    public File getOutputPath() {
        return outputPath;
    }
}
