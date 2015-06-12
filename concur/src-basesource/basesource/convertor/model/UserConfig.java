package basesource.convertor.model;

import basesource.convertor.contansts.ConfigKey;
import basesource.convertor.contansts.Configurations;
import utils.StringUtils;

import java.io.File;

/**
 * 用户配置
 * Created by Jake on 2015/6/2.
 */
public class UserConfig {

    private static final UserConfig instance = new UserConfig();

    private File outputPath;

    private File inputPath;

    private File sourceDefineInputPath;

    UserConfig() {
        this.init();
    }

    private void init() {
        String inputPath = Configurations.getConfigure(ConfigKey.FILE_SOURCE_INPUT_PATH);
        if (!StringUtils.isBlank(inputPath)) {
            File inputFile = new File(inputPath);
            if (inputFile.exists()) {
                this.inputPath = inputFile;
            }
        }

        String outputPath = Configurations.getConfigure(ConfigKey.FILE_SOURCE_OUTPUT_PATH);
        if (!StringUtils.isBlank(outputPath)) {
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                this.outputPath = outputFile;
            }
        }


        String sourceDefineInputPath = Configurations.getConfigure(ConfigKey.CODE_SOURCE_INPUT_PATH);
        if (!StringUtils.isBlank(sourceDefineInputPath)) {
            File sourceDefineInputFile = new File(sourceDefineInputPath);
            if (sourceDefineInputFile.exists()) {
                this.sourceDefineInputPath = sourceDefineInputFile;
            }
        }
    }

    public static UserConfig getInstance() {
        return instance;
    }

    public void changeOutPutPath(File outputPath) {
        this.outputPath = outputPath;
        // 保存到配置文件
        Configurations.saveConfigure(ConfigKey.FILE_SOURCE_OUTPUT_PATH, outputPath.getPath());
    }


    public void changeInPutPath(File inputPath) {
        this.inputPath = inputPath;
        // 保存到配置文件
        Configurations.saveConfigure(ConfigKey.FILE_SOURCE_INPUT_PATH, inputPath.getPath());
    }

    public void changeSourceDefineInputPath(File sourceDefineInputPath) {
        this.sourceDefineInputPath = sourceDefineInputPath;
        // 保存到配置文件
        Configurations.saveConfigure(ConfigKey.CODE_SOURCE_INPUT_PATH, sourceDefineInputPath.getPath());
    }

    public File getOutputPath() {
        return outputPath;
    }

    public File getInputPath() {
        return inputPath;
    }

    public File getSourceDefineInputPath() {
        return sourceDefineInputPath;
    }
}
