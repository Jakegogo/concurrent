//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package basesource.convertor.ui.extended;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

public class FileNode extends DefaultMutableTreeNode {
    private final File file;
    private final boolean hasChildDirectorys;
    private boolean init = false;

    public FileNode() {
        this.file = null;
        this.hasChildDirectorys = true;
    }

    public FileNode(File file, boolean hasChildDirectorys) {
        super(file);
        this.file = file;
        this.hasChildDirectorys = hasChildDirectorys;
    }

    public boolean getAllowsChildren() {
        return this.hasChildDirectorys;
    }

    public File getFile() {
        return this.file;
    }

    public boolean isLeaf() {
        return !this.hasChildDirectorys;
    }

    public String toString() {
        if (this.file == null) {
            return null;
        }
        return this.file.getName();
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public boolean hasInit() {
        return init;
    }
}
