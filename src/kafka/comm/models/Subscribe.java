package kafka.comm.models;

public class Subscribe {

	private String subscribe_id;
	private int offset;
	
	
	public Subscribe(String subscribe_id, int offset) {
		super();
		this.subscribe_id = subscribe_id;
		this.offset = offset;
	}
	public String getSubscribe_id() {
		return subscribe_id;
	}
	public void setSubscribe_id(String subscribe_id) {
		this.subscribe_id = subscribe_id;
	}
	public int getOffset() {
		return offset;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	
}
