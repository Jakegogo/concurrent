package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;

public class TestPrimitive {

	
	public static void main(String[] args) {

        ByteArray byteArray = Transfer.encode(1.23f);

        byte[] bytes = byteArray.toBytes();

        System.out.println(bytes.length);
        
        float dVal = Transfer.decode(bytes, float.class);
        
        System.out.println(dVal);
        
        System.out.println(Float.floatToRawIntBits(1.23f));
        
	}
	
	
}
