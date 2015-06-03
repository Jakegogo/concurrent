//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basesource.convertor.ui.extended;

import javax.swing.tree.DefaultMutableTreeNode;

import basesource.convertor.model.FolderInfo;

public class FileNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -937774607532009623L;
	
	private final FolderInfo folderInfo;
    private boolean init = false;

    public FileNode() {
        this.folderInfo = null;
    }

    public FileNode(FolderInfo folderInfo) {
        super(folderInfo);
        this.folderInfo = folderInfo;
    }

    public boolean getAllowsChildren() {
    	if (this.folderInfo == null) {
            return true;
        }
        return this.folderInfo.isHasChildDirectorys();
    }

    public FolderInfo getFile() {
        return this.folderInfo;
    }

    public boolean isLeaf() {
    	if (this.folderInfo == null) {
            return false;
        }
        return !this.folderInfo.isHasChildDirectorys();
    }

    public String toString() {
        if (this.folderInfo == null) {
            return null;
        }
        return this.folderInfo.getName();
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public boolean hasInit() {
        return init;
    }
}
