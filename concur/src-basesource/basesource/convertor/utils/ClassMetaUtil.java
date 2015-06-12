package basesource.convertor.utils;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.asm.ClassReader;


/**
 * 类/字节码工具
 * @author JY253
 *
 */
public class ClassMetaUtil {
	
	
	/**
	 * 获取字节码类信息
	 * @param input
	 * @return
	 */
	public static ClassMeta getClassMeta(InputStream input){
		ClassReader reader;
		try {
			reader = new ClassReader(input);
			ClassMetaVisitor clzVisitor = new ClassMetaVisitor();
			reader.accept(clzVisitor, true);
			return clzVisitor;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * 获取字节码类信息
	 * @param file
	 * @return
	 */
	public static ClassMeta getClassMeta(String file){
		ClassReader reader;
		try {
			reader = new ClassReader(new FileInputStream(file));
			ClassMetaVisitor clzVisitor = new ClassMetaVisitor();
			reader.accept(clzVisitor, true);
			return clzVisitor;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * 获取字节码类信息
	 * @param bytes
	 * @return
	 */
	public static ClassMeta getClassMeta(byte[] bytes){
		ClassReader reader;
		try {
			reader = new ClassReader(new ByteArrayInputStream(bytes));
			ClassMetaVisitor clzVisitor = new ClassMetaVisitor();
			reader.accept(clzVisitor, true);
			return clzVisitor;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
}
