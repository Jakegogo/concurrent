package basesource.convertor.utils;


import org.springframework.asm.ClassReader;

import java.io.*;


/**
 * 类/字节码工具
 * @author JY253
 *
 */
public class ClassMetaUtil {
	
	
	/**
	 * 获取字节码类信息
	 * @param input InputStream
	 * @return
	 */
	public static ClassMeta getClassMeta(InputStream input){
		try {
			byte[] bytes = toBytes(input);

			ClassReader reader = new ClassReader(bytes);
			ClassMetaVisitor clzVisitor = new ClassMetaVisitor();
			reader.accept(clzVisitor, true);

			clzVisitor.setBytes(bytes);

			return clzVisitor;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException e) {}
		}
		
		return null;
	}
	
	
	/**
	 * 获取字节码类信息
	 * @param file 文件路径字符串 E:\\
	 * @return
	 */
	public static ClassMeta getClassMeta(String file){
		FileInputStream fileInputStream = null;

		try {
			fileInputStream = new FileInputStream(file);
			byte[] bytes = toBytes(fileInputStream);

			ClassReader reader = new ClassReader(bytes);
			ClassMetaVisitor clzVisitor = new ClassMetaVisitor();
			reader.accept(clzVisitor, true);

			return clzVisitor;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e1) { }
			}
		}
		
		return null;
	}


	/**
	 * 获取字节码类信息
	 * @param file File
	 * @return
	 */
	public static ClassMeta getClassMeta(File file){
		FileInputStream fileInputStream = null;

		try {
			fileInputStream = new FileInputStream(file);
			byte[] bytes = toBytes(fileInputStream);

			ClassReader reader = new ClassReader(bytes);
			ClassMetaVisitor clzVisitor = new ClassMetaVisitor();
			reader.accept(clzVisitor, true);

			clzVisitor.setBytes(bytes);

			return clzVisitor;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e1) { }
			}
		}
		return null;
	}

	
	/**
	 * 获取字节码类信息
	 * @param bytes byte[] 类文件字节数组
	 * @return
	 */
	public static ClassMeta getClassMeta(byte[] bytes){
		ClassReader reader = new ClassReader(bytes);
		ClassMetaVisitor clzVisitor = new ClassMetaVisitor();
		reader.accept(clzVisitor, true);
		clzVisitor.setBytes(bytes);
		return clzVisitor;
	}


	/**
	 * 输入流转换成字节数组
	 * @param inputStream InputStream
	 * @return
	 * @throws IOException
	 */
	private static byte[] toBytes(InputStream inputStream) throws IOException {
		if(inputStream == null) {
			throw new IOException("Class not found");
		} else {
			byte[] var1 = new byte[inputStream.available()];
			int var2 = 0;

			while(true) {
				int var3 = inputStream.read(var1, var2, var1.length - var2);
				byte[] var4;
				if(var3 == -1) {
					if(var2 < var1.length) {
						var4 = new byte[var2];
						System.arraycopy(var1, 0, var4, 0, var2);
						var1 = var4;
					}

					return var1;
				}

				var2 += var3;
				if(var2 == var1.length) {
					var4 = new byte[var1.length + 1000];
					System.arraycopy(var1, 0, var4, 0, var2);
					var1 = var4;
				}
			}
		}
	}

	
}
