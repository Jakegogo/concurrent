package basesource.test;

import basesource.convertor.utils.ClassScanner;

import java.util.Set;

/**
 * Created by Jake on 2015/6/12.
 */
public class TestScanClass {

    public static void main(String[] args) {
        Set<Class<?>> classes = new ClassScanner().scanPath("E:\\Project\\java_old\\sanguohun2\\target\\classes");
        System.out.println(classes.size());
    }

}
