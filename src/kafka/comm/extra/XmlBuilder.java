package kafka.comm.extra;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;

public class XmlBuilder {
	public static String encode(Message data) {
		String rtn = null;

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);
			StringWriter writer = new StringWriter();
			jaxbContext.createMarshaller().marshal(data, writer);
			rtn = writer.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return rtn;
	}

	public static Message decode(String data) {
		Message rtn = null;

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Message.class);
			StringReader src = new StringReader(data);
			rtn = (Message) jaxbContext.createUnmarshaller().unmarshal(src);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return rtn;
	}

}
