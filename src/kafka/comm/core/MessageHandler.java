package kafka.comm.core;


public interface MessageHandler {
	void process(byte[] msg);
}
