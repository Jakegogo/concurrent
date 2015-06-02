package basesource.convertor.ui.extended;

import sun.awt.shell.ShellFolder;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Jake on 2015/6/1.
 */
public class CustomFileSystemView extends FileSystemView {

    static FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    public CustomFileSystemView() {
        super();
    }

    @Override
    public boolean isRoot(File f) {
        return fileSystemView.isRoot(f);
    }

    @Override
    public Boolean isTraversable(File f) {
        return fileSystemView.isTraversable(f);
    }

    @Override
    public String getSystemDisplayName(File f) {
        return fileSystemView.getSystemDisplayName(f);
    }

    @Override
    public String getSystemTypeDescription(File f) {
        return fileSystemView.getSystemTypeDescription(f);
    }

    @Override
    public boolean isParent(File folder, File file) {
        return fileSystemView.isParent(folder, file);
    }

    @Override
    public File getChild(File parent, String fileName) {
        return fileSystemView.getChild(parent, fileName);
    }

    @Override
    public boolean isFileSystem(File f) {
        return fileSystemView.isFileSystem(f);
    }

    @Override
    public boolean isHiddenFile(File f) {
        return fileSystemView.isHiddenFile(f);
    }

    @Override
    public boolean isFileSystemRoot(File dir) {
        return fileSystemView.isFileSystemRoot(dir);
    }

    @Override
    public boolean isDrive(File dir) {
        return fileSystemView.isDrive(dir);
    }

    @Override
    public boolean isFloppyDrive(File dir) {
        return fileSystemView.isFloppyDrive(dir);
    }

    @Override
    public boolean isComputerNode(File dir) {
        return fileSystemView.isComputerNode(dir);
    }

    @Override
    public File[] getRoots() {
        return fileSystemView.getRoots();
    }

    @Override
    public File getHomeDirectory() {
        return fileSystemView.getHomeDirectory();
    }

    @Override
    public File getDefaultDirectory() {
        return fileSystemView.getDefaultDirectory();
    }

    @Override
    public File createFileObject(File dir, String filename) {
        return fileSystemView.createFileObject(dir, filename);
    }

    @Override
    public File createFileObject(String path) {
        return fileSystemView.createFileObject(path);
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        return fileSystemView.getFiles(dir, useFileHiding);
    }

    @Override
    public File getParentDirectory(File dir) {
        return fileSystemView.getParentDirectory(dir);
    }


    private static CustomFileSystemView instance = new CustomFileSystemView();

    public static CustomFileSystemView getFileSystemView() {
        return instance;
    }

    /**
     * Icon for a file, directory, or folder as it would be displayed in
     * a system file browser. Example from Windows: the "M:\" directory
     * displays a CD-ROM icon.
     *
     * The default implementation gets information from the ShellFolder class.
     *
     * @param f a <code>File</code> object
     * @return an icon as it would be displayed by a native file chooser
     * @see javax.swing.JFileChooser#getIcon
     * @since 1.4
     */
    public Icon getSystemIcon(File f) {
        if (f == null) {
            return null;
        }

        ShellFolder sf;

        try {
            sf = getShellFolder(f);
        } catch (FileNotFoundException e) {
            return null;
        }

        Image img = sf.getIcon(false);

        if (img != null) {
            return new ImageIcon(img, sf.getFolderType());
        } else {
            return UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
        }
    }


    public Image getSystemIconImage(File f) {
        if (f == null) {
            return null;
        }

        ShellFolder sf;

        try {
            sf = getShellFolder(f);
        } catch (FileNotFoundException e) {
            return null;
        }

        Image img = sf.getIcon(false);

        if (img != null) {
            return img;
        }
        return null;
    }


    ShellFolder getShellFolder(File f) throws FileNotFoundException {
        if (!(f instanceof ShellFolder) && !(f instanceof FileSystemRoot) && isFileSystemRoot(f)) {
            f = createFileSystemRoot(f);
        }

        try {
            return ShellFolder.getShellFolder(f);
        } catch (InternalError e) {
            System.err.println("FileSystemView.getShellFolder: f="+f);
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public File createNewFolder(File containingDir) throws IOException {
        return fileSystemView.createNewFolder(containingDir);
    }

    static class FileSystemRoot extends File {
        public FileSystemRoot(File f) {
            super(f, "");
        }

        public FileSystemRoot(String s) {
            super(s);
        }

        public boolean isDirectory() {
            return true;
        }

        public String getName() {
            return getPath();
        }
    }

}
