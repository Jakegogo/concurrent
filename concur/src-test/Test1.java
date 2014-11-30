import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2014/11/19.
 */
public class Test1 {

    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(Integer.valueOf(1));

        System.out.println(list.toArray());
        System.out.println(list.toArray());
        System.out.println(list.toArray().getClass());

//        List<Integer> list1 = new ArrayList<Integer>(list);

        new HashMap(new HashMap());

    }

}
