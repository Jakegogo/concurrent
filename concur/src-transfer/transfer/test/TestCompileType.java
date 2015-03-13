package transfer.test;

import transfer.ByteArray;
import transfer.Transfer;
import transfer.TypeReference;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TestCompileType {
	
	
	public static void main(String[] args) {
		
		TypeReference<Map<String,Integer>> typeReference = new TypeReference<Map<String,Integer>>(){};
		Type type = typeReference.getType();
		
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		map.put("1", 1);
		map.put("2", 2);
		map.put("3", 3);
		
		
		ByteArray byteArray = Transfer.encode(map, type);
		
		Map<String,Integer> dmap1 = Transfer.decode(byteArray, typeReference);
		
		for (Map.Entry<String, Integer> entry : dmap1.entrySet()) {
			System.out.println(entry.getKey() + "->" + entry.getValue());
		}
		
		
		
		byteArray = Transfer.encode(map, type);
		
		Map<String,Integer> dmap2 = Transfer.decode(byteArray, Map.class);
		
		for (Map.Entry<String, Integer> entry : dmap2.entrySet()) {
			System.out.println(entry.getKey() + "->" + entry.getValue());
		}
		
	}
	

}
