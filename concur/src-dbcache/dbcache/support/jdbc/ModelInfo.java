package dbcache.support.jdbc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entity信息
 * Created by Jake on 2015/1/12.
 */
public class ModelInfo {

    // 表结果信息
    private TableInfo tableInfo;

    // 属性信息
    private Map<String, ColumnInfo> attrTypeMap = new LinkedHashMap<String, ColumnInfo>();

    


}
