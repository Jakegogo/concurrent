package basesource.convertor.utils;

public interface ClassMeta {

	String getClassName();

	int getVersion();

	int getAccess();

	String getSuperName();

	String[] getInterfaces();

	String getSignature();

	byte[] getBytes();
}
