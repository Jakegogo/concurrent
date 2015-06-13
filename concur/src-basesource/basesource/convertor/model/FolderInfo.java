package basesource.convertor.model;

import javax.swing.*;
import java.io.File;

/**
 * 文件夹信息
 * @author Jake
 *
 */
public class FolderInfo {
	
	/**
	 * 文件名称
	 */
	private String name;
	
	/**
	 * 文件图标
	 */
	private Icon icon;
	
	/**
	 * 文件路径
	 */
	private String path;
	
	/**
	 * 是否有子目录
	 */
	private boolean hasChildDirectorys;
	
	/**
	 * 文件缓存
	 */
	private File fileCache;


	// --- get/set ---

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isHasChildDirectorys() {
		return hasChildDirectorys;
	}

	public void setHasChildDirectorys(boolean hasChildDirectorys) {
		this.hasChildDirectorys = hasChildDirectorys;
	}

	public File getFileCache() {
		return fileCache;
	}

	public void setFileCache(File fileCache) {
		this.fileCache = fileCache;
	}

	
}
