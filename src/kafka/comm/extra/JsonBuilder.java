package kafka.comm.extra;

import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * construct data representations using JSON
 * 
 * @author gash1
 * 
 */
public class JsonBuilder {
	public static String encode(HashMap<String, String> data) {
		try {
			var mapper = new ObjectMapper();
			return mapper.writeValueAsString(data);
		} catch (Exception ex) {
			return null;
		}
	}

	public static String encode(Object data) {
		try {
			var mapper = new ObjectMapper();
			return mapper.writeValueAsString(data);
		} catch (Exception ex) {
			return null;
		}
	}

	public static <T> T decode(String data, Class<T> theClass) {
		try {
			var mapper = new ObjectMapper();
			return mapper.readValue(data.getBytes(), theClass);
		} catch (Exception ex) {
			return null;
		}
	}
}
