package basesource.convertor.task;

import basesource.convertor.model.ProgressTableModel;
import basesource.convertor.model.UserConfig;
import basesource.convertor.utils.ClassScanner;
import basesource.reader.ExcelReader;
import basesource.storage.FormatDefinition;
import basesource.storage.ResourceDefinition;
import basesource.storage.Storage;
import basesource.storage.StorageManager;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import utils.JsonUtils;
import utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 转换任务
 * Created by Jake on 2015/6/11.
 */
public class ConvertTask implements ProgressMonitorAble {

    /**
     * 文件夹路径
     */
    File path;

    /**
     * 表格模型
     */
    ProgressTableModel tableModel;

    /**
     * 当前进度的文件迭代
     */
    private Iterator<File> cur;

    /**
     * 当前任务序号 从0开始
     */
    private volatile int curTaskIndex = 0;

    /**
     * 基础数据缓存管理器
     */
    private StorageManager storageManager;

    /**
     * 是否暂停
     */
    private volatile boolean stop = false;


    public ConvertTask(File path, ProgressTableModel tableModel) {
        this.path = path;
        this.tableModel = tableModel;
        this.storageManager = new StorageManager();
    }


    /**
     * 开始任务
     */
    public void start() {

        // 读取基础数据定义类
        Map<String, Class<?>> loadedClassMap = loadCodeSource();

        // 创建迭代器
        if (cur == null) {
            cur = tableModel.getSortedRowFiles().iterator();
            curTaskIndex = 0;

            // 重置进度
            tableModel.clearProgress();
        }
        stop = false;

        // 开始转换
        while (cur.hasNext()) {
            if (stop) {
                return;
            }
            File file = cur.next();
            try {
                runSubTask(file, loadedClassMap);
            } catch (RuntimeException e) {
                System.err.println("文件转换失败:" + file.getName());
                e.printStackTrace();
            }
            // 自增任务序号
            curTaskIndex ++;
        }

        // 验证数据 TODO


        // 销毁迭代器
        cur = null;
    }


    /**
     * 暂停任务
     */
    public void stop() {
        stop = true;
    }

    /**
     * 是否为暂停状态
     * @return
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * 重置任务
     */
    public void reset() {
        // 重置迭代器
        cur = tableModel.getSortedRowFiles().iterator();
        curTaskIndex = 0;

        // 重置进度
        tableModel.clearProgress();
    }


    /**
     * 改变输入路径
     * @param path
     */
    public void changeInputPath(File path) {
        this.reset();
        this.path = path;

        // 重置进度
        tableModel.clearProgress();
    }

    /**
     * 开始
     * @param file
     */
    private void runSubTask(File file, Map<String, Class<?>> loadedClassMap) {

        Workbook workbook = getWorkbook(file);
        updateProgress(0.37d);
        Map<String, SheetInfo> sheets = listSheets(workbook, file);

        int size = sheets.size();
        int curSheetIndex = 0;

        for (Map.Entry<String, SheetInfo> entry : sheets.entrySet()) {
            String name = entry.getKey();
            SheetInfo sheetInfo = entry.getValue();

            if (loadedClassMap.containsKey(name)) {
                Class<?> cls = loadedClassMap.get(name);
                // 创建基础数据资源定义
                ResourceDefinition resourceDefinition = createResourceDefinition(cls, loadedClassMap, name, sheetInfo.file);
                this.storageManager.initialize(resourceDefinition);

                Storage<?, ?> storage = this.storageManager.getStorage(cls);
                Collection<?> beanList = storage.getAll();

                this.writeFile(name, JsonUtils.object2JsonString(beanList));
            } else {
                // 创建数据集
                List<Map<String, String>> beanList = readSheetData(sheetInfo, ((double)curSheetIndex + 1) / size * (1-0.37d) + 0.37d);

                this.writeFile(name, JsonUtils.object2JsonString(beanList));
            }

            curSheetIndex ++;
            updateProgress(((double)curSheetIndex) / size * (1-0.37d) + 0.37d);
        }
        updateProgress(1d);
    }


    // 改变转换进度
    public void updateProgress(double v) {
        this.tableModel.changeProgress(curTaskIndex, v);
    }


    // 读取表格内容
    private List<Map<String, String>> readSheetData(SheetInfo sheetInfo, double maxProgress) {
        List<Map<String, String>> beanList = new ArrayList<Map<String, String>>();
        Collection<ColumnInfo> infos = getColumnInfo(sheetInfo.sheets.get(0));

        int size = sheetInfo.sheets.size();
        int curSheetIndex = 0;

        for (Sheet sheet : sheetInfo.sheets) {
            boolean start = false;

            int rowSize = sheet.getLastRowNum();
            int curRowIndex = 0;

            for (Row row : sheet) {

                // 判断数据行开始没有
                if (!start) {
                    Cell cell = row.getCell(0);
                    if (cell == null) {
                        continue;
                    }
                    String content = getCellContent(cell);
                    if (content == null) {
                        continue;
                    }
                    if (content.equals(ExcelReader.ROW_SERVER)) {
                        start = true;
                    }
                    continue;
                }

                // 生成返回对象
                Map<String, String> object = new HashMap<String, String>();
                for (ColumnInfo info : infos) {
                    Cell cell = row.getCell(info.column);
                    if (cell == null) {
                        continue;
                    }
                    String content = getCellContent(cell);
                    if (StringUtils.isEmpty(content)) {
                        continue;
                    }
                    object.put(info.name, content);
                }
                beanList.add(object);

                // 结束处理
                Cell cell = row.getCell(0);
                if (cell == null) {
                    continue;
                }
                String content = getCellContent(cell);
                if (content == null) {
                    continue;
                }
                if (content.equals(ExcelReader.ROW_END)) {
                    break;
                }

                curRowIndex ++;
                updateProgress(((double)curRowIndex) / rowSize * (((double)curSheetIndex) / size) * maxProgress);

            }

            curSheetIndex ++;
            updateProgress(((double)curSheetIndex) / size * maxProgress);

        }

        return beanList;
    }


    // 保存到文件
    private void writeFile(String name, String content) {
        String path = UserConfig.getInstance().getOutputPath().getAbsolutePath();
        String fileName = path + File.separator + name + ".json";
        try {
            FileUtils.writeStringToFile(new File(fileName), content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 创建基础数据资源定义
    private ResourceDefinition createResourceDefinition(Class<?> cls, Map<String, Class<?>> loadedClassMap, String name, File file) {
        // 获取后缀
        String fileName = file.getName();
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1,fileName.length());

        // 获取定义
        FormatDefinition formatDefinition = new FormatDefinition("file:" + file.getAbsolutePath(), "excel", fileType);
        return new ResourceDefinition(cls, formatDefinition);
    }


    /**
     * 获取基础数据定义的类
     * @return
     */
    private Map<String, Class<?>> loadCodeSource() {
        Map<String, Class<?>> loadedClassMap = null;
        File sourceDefineInputPath = UserConfig.getInstance().getSourceDefineInputPath();
        if (sourceDefineInputPath != null && sourceDefineInputPath.exists()) {
            ClassScanner classScanner = new ClassScanner();
            Set<Class<?>> loadedClass = classScanner.scanPath(sourceDefineInputPath.getAbsolutePath());

            loadedClassMap = new HashMap<String, Class<?>>();
            for (Class<?> cls : loadedClass) {
                loadedClassMap.put(cls.getSimpleName(), cls);
            }
        }
        return loadedClassMap;
    }


    /**
     * 获取资源类型对应的工作簿
     * @param wb Excel Workbook
     * @return 基础班名称 - List<Sheet>
     */
    private Map<String, SheetInfo> listSheets(Workbook wb, File file) {
        try {
            Map<String, SheetInfo> result = new HashMap<String, SheetInfo>();
            // 处理多Sheet数据合并
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                if (sheet.getLastRowNum() <= 0) {
                    continue;
                }
                Row row = sheet.getRow(0);
                if (row == null) {
                    continue;
                }
                if (row.getLastCellNum() <= 0) {
                    continue;
                }
                Cell cell = row.getCell(0);
                if (cell == null) {
                    continue;
                }
                // 获取属性控制行
                Row fieldRow = getFieldRow(sheet);
                if (fieldRow == null) {
                    continue;
                }
                if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                }
                String text = cell.getStringCellValue();
                if (StringUtils.isBlank(text)) {
                    continue;
                }

                SheetInfo sheetInfo = result.get(text);
                if (sheetInfo == null) {
                    sheetInfo = new SheetInfo();
                    sheetInfo.file = file;
                    sheetInfo.sheets = new ArrayList<Sheet>();
                    result.put(text, sheetInfo);
                }
                sheetInfo.sheets.add(sheet);
            }

            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("无法获取资源类[" + file.getPath() + "]对应的Excel数据表", e);
        }
    }


    /**
     * 获取列信息
     * @param sheet
     * @return
     */
    private List<ColumnInfo> getColumnInfo(Sheet sheet) {
        // 获取属性控制行
        Row fieldRow = getFieldRow(sheet);
        if (fieldRow == null) {
            FormattingTuple message = MessageFormatter.format("无法获取资源[ {} ]的EXCEL文件的属性控制列", sheet.getSheetName());
            throw new IllegalStateException(message.getMessage());
        }

        // 获取属性信息集合
        List<ColumnInfo> result = new ArrayList<ColumnInfo>();
        for (int i = 1; i < fieldRow.getLastCellNum(); i++) {
            Cell cell = fieldRow.getCell(i);
            if (cell == null) {
                continue;
            }

            String name = getCellContent(cell);
            if (StringUtils.isBlank(name)) {
                continue;
            }

            ColumnInfo info = new ColumnInfo();
            info.name = name;
            info.column = i;
            result.add(info);
        }
        return result;
    }


    /**
     * 通过输入流获取{@link org.apache.poi.ss.usermodel.Workbook}
     * @param file
     * @return
     */
    private Workbook getWorkbook(File file) {
        try {
            return WorkbookFactory.create(new ProgressMonitorInputStream(new FileInputStream(file), 0.1f) {
                @Override
                public void updateProgress(double progresss) {
                    ConvertTask.this.updateProgress(0.37d * progresss);
                }
            });
        } catch (InvalidFormatException e) {
            throw new RuntimeException("静态资源[" + file.getPath() + "]异常,无效的文件格式", e);
        } catch (IOException e) {
            throw new RuntimeException("静态资源[" + file.getPath() + "]异常,无法读取文件", e);
        }
    }


    /**
     * 获取属性控制行
     * @param sheet
     * @return
     */
    private Row getFieldRow(Sheet sheet) {
        for (Row row : sheet) {
            Cell cell = row.getCell(0);
            if (cell == null) {
                continue;
            }
            String content = getCellContent(cell);
            if (content != null && content.equals(ExcelReader.ROW_SERVER)) {
                return row;
            }
        }
        return null;
    }


    /**
     * 获取字符串形式的单元格内容
     * @param cell
     * @return
     */
    private String getCellContent(Cell cell) {
        if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
        return cell.getStringCellValue();
    }

    public File getPath() {
        return path;
    }


    /**
     * 表格信息
     */
    static class SheetInfo {

        List<Sheet> sheets;

        File file;

    }

    /**
     * 表格列信息
     */
    static class ColumnInfo {

        String name;

        int column;

    }

}
