package dbcache.test;


import com.alibaba.fastjson.TypeReference;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2015/1/3.
 */
public class TestJson {


    public static void main(String[] args) {

        Type type = new TypeReference<ConcurrentMap<String, Object>>(){}.getType();

        System.out.println(type);
        
    }

}
