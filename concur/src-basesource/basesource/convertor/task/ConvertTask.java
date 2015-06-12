package basesource.convertor.task;

import basesource.convertor.model.ProgressTableModel;
import basesource.convertor.utils.ClassScanner;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 转换任务
 * Created by Jake on 2015/6/11.
 */
public class ConvertTask {

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

    public ConvertTask(File path, ProgressTableModel tableModel) {
        this.path = path;
        this.tableModel = tableModel;
    }


    /**
     * 开始任务
     */
    public void start() {

        ClassScanner classScanner = new ClassScanner();

        cur = tableModel.getSortedRowFiles().iterator();
        runSubTask(cur.next());
    }

    /**
     * 开始
     * @param file
     */
    private void runSubTask(File file) {

        Workbook workbook = getWorkbook(file);
        Map<String, List<Sheet>> sheets = listSheets(workbook, file);




    }


    /**
     * 获取资源类型对应的工作簿
     * @param wb Excel Workbook
     * @return
     */
    private Map<String, List<Sheet>> listSheets(Workbook wb, File file) {
        try {
            Map<String, List<Sheet>> result = new HashMap<String, List<Sheet>>();
            // 处理多Sheet数据合并
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                if (sheet.getLastRowNum() <= 0) {
                    continue;
                }
                Row row = sheet.getRow(0);
                if (row.getLastCellNum() <= 0) {
                    continue;
                }
                Cell cell = row.getCell(0);
                if (cell == null) {
                    continue;
                }
                if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                }
                String text = cell.getStringCellValue();

                List<Sheet> list = result.get(text);
                if (list == null) {
                    list = new ArrayList<Sheet>();
                    result.put(text, list);
                }
                list.add(sheet);
            }

            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("无法获取资源类[" + file.getPath() + "]对应的Excel数据表", e);
        }
    }


    /**
     * 通过输入流获取{@link org.apache.poi.ss.usermodel.Workbook}
     * @param file
     * @return
     */
    private Workbook getWorkbook(File file) {
        try {
            return WorkbookFactory.create(new FileInputStream(file));
        } catch (InvalidFormatException e) {
            throw new RuntimeException("静态资源[" + file.getPath() + "]异常,无效的文件格式", e);
        } catch (IOException e) {
            throw new RuntimeException("静态资源[" + file.getPath() + "]异常,无法读取文件", e);
        }
    }


    public File getPath() {
        return path;
    }

}
