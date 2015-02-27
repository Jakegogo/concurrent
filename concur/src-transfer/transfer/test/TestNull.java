package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;

/**
 * Created by Administrator on 2015/2/26.
 */
public class TestNull {

    public static void main(String[] args) {


        System.out.println(null instanceof Class<?>);


        ByteArray byteArray = Transfer.encode(null);

        byte[] bytes = byteArray.toBytes();

        System.out.println(bytes.length);

    }

}
