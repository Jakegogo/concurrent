package transfer.test;

import transfer.TypeReference;

import java.util.Map;

/**
 * Created by Jake on 2015/3/7.
 */
public class TestType {


    public static void main(String[] args) {

        System.out.println(new TypeReference<Entity>(){}.getType() == new TypeReference<Entity>(){}.getType());

        System.out.println(new TypeReference<Map<String,Entity>>(){}.getType() == new TypeReference<Map<String,Entity>>(){}.getType());

        System.out.println(new TypeReference<Map<String,Entity>>(){}.getType().hashCode());

        System.out.println(new TypeReference<Map<String,Entity>>(){}.getType().hashCode());
        
        System.out.println(System.identityHashCode(new TypeReference<Map<String,Entity>>(){}.getType()));

        System.out.println(System.identityHashCode(new TypeReference<Map<String,Entity>>(){}.getType()));
        
    }


}
