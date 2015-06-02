package basesource.convertor.files;

import basesource.convertor.files.monitor.FileAlterationListener;
import basesource.convertor.files.monitor.FileAlterationObserver;
import basesource.convertor.model.ListableFileObservable;

import java.io.File;

/**
 * 文件更新监听
 */
public class UserFileListener implements FileAlterationListener {

    private ListableFileObservable listableFileConnector;

    public UserFileListener(ListableFileObservable listableFileConnector) {
        this.listableFileConnector = listableFileConnector;
    }

    @Override
    public void onStart(FileAlterationObserver observer) {

    }

    @Override
    public void onDirectoryCreate(File directory) {

    }

    @Override
    public void onDirectoryChange(File directory) {

    }

    @Override
    public void onDirectoryDelete(File directory) {
        this.listableFileConnector.updateSelectDirectory();
    }

    @Override
    public void onFileCreate(File file) {
        this.listableFileConnector.updateSelectDirectory();
    }

    @Override
    public void onFileChange(File file) {

    }

    @Override
    public void onFileDelete(File file) {
        this.listableFileConnector.updateSelectDirectory();
    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }
}