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

    /** 输出文件夹 */
    private File outputPath;

    /** 输入文件夹 */
    private File inputPath;

    /** 基础数据定义代码路径 */
    private File sourceDefineInputPath;

    UserConfig() {
        this.init();
    }

    /**
     * 初始化配置
     */
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

    // 获取实例
    public static UserConfig getInstance() {
        return instance;
    }

    /**
     * 修改输出路径
     * @param outputPath File
     */
    public void changeOutPutPath(File outputPath) {
        this.outputPath = outputPath;
        // 保存到配置文件
        Configurations.saveConfigure(ConfigKey.FILE_SOURCE_OUTPUT_PATH, outputPath.getPath());
    }


    /**
     * 修改输入路径
     * @param inputPath File
     */
    public boolean changeInPutPath(File inputPath) {
        if (inputPath == null || !inputPath.exists()) {
            return false;
        }
        boolean isChanged = !inputPath.equals(this.inputPath);

        this.inputPath = inputPath;
        // 保存到配置文件
        Configurations.saveConfigure(ConfigKey.FILE_SOURCE_INPUT_PATH, inputPath.getPath());

        return isChanged;
    }

    /**
     * 修改基础数据定义代码路径
     * @param sourceDefineInputPath File
     */
    public void changeSourceDefineInputPath(File sourceDefineInputPath) {
        this.sourceDefineInputPath = sourceDefineInputPath;
        // 保存到配置文件
        Configurations.saveConfigure(ConfigKey.CODE_SOURCE_INPUT_PATH, sourceDefineInputPath.getPath());
    }

    // --- get/set ---

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
