package basesource.convertor.model;

import basesource.convertor.contansts.DefaultUIConstant;
import basesource.convertor.tools.WindowsShortcut;
import basesource.convertor.ui.extended.FileNode;
import sun.awt.shell.ShellFolder;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文件管理器
 * Created by Jake on 2015/6/2.
 */
@SuppressWarnings("restriction")
public class ListableFileManager {

    FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    /**
     * 可用的文件格式
     */
    private Set<String> validFileExtension = new HashSet<String>();
    {
        validFileExtension.add("xls");
        validFileExtension.add("xlsx");
    }


    /**
     * 获取文件夹的子目录
     * @param file
     * @return
     */
    public List<File> listChildFolders(File file) {
        List<File> fileList = new ArrayList<File>();
        if (file.isDirectory()) {
            File[] files = fileSystemView.getFiles(file, true); //!!
            for (File child : files) {
                if (child.isDirectory()) {
                    fileList.add(child);
                }
            }
        }
        return fileList;
    }

    
    /**
     * 获取文件夹的子目录
     * @param folderInfo
     * @return
     */
    public List<FolderInfo> listChildFolderInfo(FolderInfo folderInfo) {
    	
    	File file = folderInfo.getFileCache();
        if (!file.exists()) {
        	return Collections.emptyList();
        }
    	
        List<FolderInfo> fileList = new ArrayList<FolderInfo>();
        File[] files = fileSystemView.getFiles(file, true); //!!
        for (File child : files) {
            if (child.isDirectory()) {
                fileList.add(this.generateFolderInfo(child));
            }
        }
        return fileList;
    }

    /**
     * 是否还有子文件夹
     * @param file
     * @return
     */
    public boolean hasChildFolders(File file) {
        if (!file.isDirectory()) {
            return false;
        }
        File[] files = fileSystemView.getFiles(file, true); //!!
        for (File child : files) {
            if (child.isDirectory()) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取文件夹的子文件
     * @param file
     * @return
     */
    public List<File> listChildFiles(File file) {
        List<File> fileList = new ArrayList<File>();
        if (file.isDirectory()) {
            File[] files = fileSystemView.getFiles(file, true); //!!
            for (File child : files) {
                if (child.isDirectory()) {
                    continue;
                }
                fileList.add(child);
            }
        }
        return fileList;
    }


    /**
     * 获取文件夹的子文件
     * @param file
     * @return
     */
    public List<File> filterChildFiles(File file) {
        List<File> fileList = new ArrayList<File>();
        if (file.isDirectory()) {
            File[] files = fileSystemView.getFiles(file, true); //!!
            for (File child : files) {
                if (child.isDirectory()) {
                    continue;
                }
                if (!validFileExtension(child)) {
                    continue;
                }
                fileList.add(child);
            }
        }
        return fileList;
    }


    private boolean validFileExtension(File child) {
        return validFileExtension.contains(getFileExtension(child));
    }


    /**
     * 获取文件图标
     * @param file
     * @return
     */
    public Icon getSystemIcon(File file) {
        return fileSystemView.getSystemIcon(file);
    }


    /**
     * 获取文件名称
     * @param file
     * @return
     */
    public String getSystemDisplayName(File file) {
        return fileSystemView.getSystemDisplayName(file);
    }


    /**
     * 获取默认文件选择列表
     * @param root
     */
    public void convertDefaultFileListToTreeNode(FileNode root) {
    	
        // show the file system roots. 显示全部文件夹
        File[] roots = fileSystemView.getRoots();
        for (File fileSystemRoot : roots) {
            FileNode node = new FileNode(this.generateFolderInfo(fileSystemRoot));
            root.add(node);

            File[] files = fileSystemView.getFiles(fileSystemRoot, true);
            for (File file : files) {
                if (file.isDirectory()) {
                    FileNode fileNode = new FileNode(this.generateFolderInfo(file));
                    node.add(fileNode);
                }
            }
        }

    }

    
    // 生成文件夹信息
    private FolderInfo generateFolderInfo(File file) {
    	FolderInfo folderInfo = new FolderInfo();
    	folderInfo.setName(fileSystemView.getSystemDisplayName(file));
    	folderInfo.setIcon(fileSystemView.getSystemIcon(file));
    	
    	File realFile = this.parseDirectory(file);
    	folderInfo.setPath(realFile.getPath());
    	folderInfo.setHasChildDirectorys(hasChildFolders(realFile));
    	folderInfo.setFileCache(realFile);;
    	
		return folderInfo;
	}

    
    // 解析快捷方式
	public File parseDirectory(File file) {
    	if (file instanceof ShellFolder) {
    		ShellFolder folder =  (ShellFolder) file;
    		if (folder.isLink() && file.getName().toLowerCase().endsWith(".lnk")) {
    			try {
    				WindowsShortcut windowsShortcut = new WindowsShortcut(file, DefaultUIConstant.FILE_NAME_ENCODE);
    				return new File(windowsShortcut.getRealFilename());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
    		}
    	}
		return file;
	}


	/**
     * 使用默认程序打开文件
     * @param file
     */
    public void openFileView(File file) {
        if (file == null) {
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    /**
     * 获取文件扩展名
     * @param file
     * @return
     */
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

}
