package basesource.reader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import basesource.exceptions.DecodeException;


/**
 * JSON 资源读取器
 * @author frank
 */
public class JsonReader implements ResourceReader {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	private static final TypeFactory typeFactory = TypeFactory.defaultInstance();

	public <E> Iterator<E> read(InputStream input, Class<E> clz) {
		try {
			JavaType type = typeFactory.constructCollectionType(ArrayList.class, clz);
			// JavaType type = TypeFactory.collectionType(ArrayList.class, clz);
			List<E> list = mapper.readValue(input, type);
			return list.iterator();
		} catch (Exception e) {
			throw new DecodeException(e);
		}
	}

	@Override
	public String getFormat() {
		return "json";
	}

}
