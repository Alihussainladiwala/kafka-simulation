package kafka.comm.extra;

import java.util.Date;
import java.util.List;

import kafka.TopicMessage;
import kafka.comm.payload.MessageBuilder.MessageType;

public class Message {
	// header info
	private MessageType type;
	private String mid;
	private String source;
	//private Date sent;
	private Date received;
	private String Status;
	private String destination;
	private List<String> peers;
	private TopicMessage message;
	private TopicMessage topicMessage;

	public TopicMessage getTopicMessage() {
		return topicMessage;
	}

	public void setTopicMessage(TopicMessage topicMessage) {
		this.topicMessage = topicMessage;
	}

	public List<String> getPeers() {
		return peers;
	}

	public void setPeers(List<String> peers) {
		this.peers = peers;
	}

	public String getStatus() {
		return Status;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public void setStatus(String status) {
		Status = status;
	}

	// message body
	private String payload;

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getReceived() {
		return received;
	}

	public void setReceived(Date received) {
		this.received = received;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String body) {
		this.payload = body;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.source).append("(").append(this.type).append(",").append(this.mid).append("): ")
				.append(this.payload);
		return sb.toString();
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}
	
}
