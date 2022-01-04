package kafka.comm.payload;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kafka.comm.extra.Message;

public class BasicBuilder extends MessageBuilder {

	protected static final String sMsgMarkerStart = "[";
	protected static final String sMsgMarkerEnd = "]";
	protected static final String sHeaderMarker = "!h!";
	protected static final String sBodyMarker = "!b!";

	protected static final String sMsgMarkerStartRX = "\\[";

	String _incompleteBuffer;

	public BasicBuilder() {
	}

	@Override
	public String encode(MessageType type, String msgId, String source, String body, String dest, Date received) {
		String payload = null;
		if (body != null)
			payload = body.trim();

		var fmt = new DecimalFormat("0000");
		var sb = new StringBuilder();
		sb.append(sHeaderMarker);
		sb.append(type);
		sb.append(',');
		sb.append(msgId);
		sb.append(',');
		sb.append(System.currentTimeMillis());
		sb.append(',');
		if (received != null)
			sb.append(received);
		sb.append(',');
		sb.append(',');
		if (source != null)
			sb.append(source.trim());
		sb.append(',');
	
			sb.append(""+dest);
		sb.append(',');
		if (payload == null) {
			sb.append(0);
			sb.append(sBodyMarker);
		} else {
			sb.append(fmt.format(payload.length()));
			sb.append(sBodyMarker);
			if (payload != null)
				sb.append(payload);
		}

		var msg = sb.toString();

		sb = new StringBuilder();
		sb.append(sMsgMarkerStart);

		sb.append(fmt.format(msg.length()));
		sb.append(msg);
		sb.append(sMsgMarkerEnd);

		return sb.toString();
	}

	@Override
	public List<Message> decode(byte[] raw) throws Exception {
		if (raw == null || raw.length == 0)
			return null;

		String s = new String(raw);
		if (_incompleteBuffer != null) {
			s = _incompleteBuffer + s;
		}

		String[] msgs = s.split(sMsgMarkerStartRX);
		var rtn = new ArrayList<Message>();
		for (String m : msgs) {
			if (m.length() == 0)
				continue;

			// incomplete message
			if (!m.endsWith(sMsgMarkerEnd)) {
				_incompleteBuffer = sMsgMarkerStart + m;
				break;
			} else
				_incompleteBuffer = null;

			// TODO use slf4j
			if (isVerbose())
				System.out.println("--> m (size = " + m.length() + "): " + m);

			String[] hdr = m.split(sHeaderMarker);
			if (hdr.length != 2)
				throw new RuntimeException("Unexpected message format");

			// TODO handle condition where the message size does not match. Note
			// end marker

			String t = hdr[1];
			String[] bd = t.split(sBodyMarker);
			if (bd.length != 2)
				throw new RuntimeException("Unexpected message format (2)");

			String body = bd[1].substring(0, bd[1].length() - 1);
			String header = bd[0];

			String[] hparts = header.split(",");
			if (hparts.length != 8)
				throw new RuntimeException("Unexpected message format (6)");

			var bo = new Message();
			bo.setType(MessageType.valueOf(hparts[0]));

			bo.setMid(hparts[1]);

			if (hparts[2].length() > 0)
				bo.setReceived(new Date(Long.parseLong(hparts[2])));
			if(!hparts[6].isEmpty()) {
				bo.setDestination(hparts[6]);
			}
			// entry 2 is not used

			bo.setSource(hparts[5]);
			

			int bodySize = Integer.parseInt(hparts[7]);
			if (bodySize != body.length())
				throw new RuntimeException("Body does not match checksum");

			bo.setPayload(body);

			// TODO use slf4j
			if (isVerbose()) {
				System.out.println("--> h: " + header);
				System.out.println("--> b: " + body);
			}

			rtn.add(bo);
		}

		return rtn;
	}

}
