package dbcache.support.jackson;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * toString()转换器
 * @author jake
 *
 */
public class ToStringJsonSerializer extends JsonSerializer<Object> {

	@Override
	public void serialize(Object value, JsonGenerator jgen,
			SerializerProvider sp) throws IOException, JsonProcessingException {
		jgen.writeString(String.valueOf(value));
	}

}