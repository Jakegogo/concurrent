package basesource.storage;

import basesource.anno.Resource;
import utils.StringUtils;

import java.io.File;

/**
 * 格式信息定义
 * 
 * @author frank
 */
public class FormatDefinition {

	public final static String FILE_SPLIT = ".";
	public final static String FILE_PATH = File.separator;

	private final String location;
	private final String type;
	private final String suffix;

	/**
	 * 构造方法
	 * @param location 绝对路径
	 * @param type 类型
	 * @param suffix 文件后缀
	 */
	public FormatDefinition(String location, String type, String suffix) {
		this.location = location;
		this.type = type;
		this.suffix = suffix;
	}


	/**
	 * 构造方法
	 * @param clz 基础数据类
	 * @param path 相对路径(user.dir)
	 * @param type 类型
	 * @param suffix 文件后缀
	 */
	public FormatDefinition(Class<?> clz, String path, String type, String suffix) {
		this.type = type;
		this.suffix = suffix;

		Resource anno = clz.getAnnotation(Resource.class);

		StringBuilder builder = new StringBuilder();
		builder.append(path).append(FILE_PATH);
		if (anno != null && !StringUtils.isBlank(anno.value())) {
			String dir = anno.value();
			int start = 0;
			int end = dir.length();
			if (dir.startsWith(FILE_PATH)) {
				start++;
			}
			if (dir.endsWith(FILE_PATH)) {
				end--;
			}
			builder.append(dir.substring(start, end)).append(FILE_PATH);
		}
		builder.append(clz.getSimpleName()).append(FILE_SPLIT).append(suffix);

		this.location = builder.toString();
	}

	// Getter and Setter ...

	public String getLocation() {
		return location;
	}

	public String getType() {
		return type;
	}

	public String getSuffix() {
		return suffix;
	}

}
