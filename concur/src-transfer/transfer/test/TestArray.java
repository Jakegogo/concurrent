package transfer.test;

/**
 * Created by Administrator on 2015/2/25.
 */
public class TestArray {

    public static void main(String[] args) {
        Object[] array = new Integer[1];
        array[0] = new Integer(1);

        for (Object obj : array) {
            System.out.println(obj);
        }

    }

}
