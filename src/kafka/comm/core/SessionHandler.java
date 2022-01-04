package kafka.comm.core;

import java.io.BufferedInputStream;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kafka.MasterService;
import kafka.TopicMessage;
import kafka.comm.extra.JsonBuilder;
import kafka.comm.extra.Message;
import kafka.comm.payload.BasicBuilder;
import kafka.comm.payload.MessageBuilder;
import kafka.comm.payload.MessageBuilder.MessageType;

/**
 * 
 * @author gash
 * 
 */
class SessionHandler extends Thread {
	private Socket _connection;
	private long _id;
	private String _name;
	private long _lastContact;
	private long _count = 0;
	private boolean _forever = true;
	private int _timeout = 10 * 1000; // 10 seconds
	private BufferedInputStream _inSock = null;
	private Sessions _sessions;
	private MessageBuilder _msgBuilder;
	private JsonBuilder _json ;
	private MasterService _masterService;
	private boolean _verbose = true;

	public SessionHandler(Socket connection, long id) {
		this._connection = connection;
		this._id = id;
		_masterService = new MasterService();
		// allow server to exit if
		this.setDaemon(true);
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append(_name).append(" - Idle: ").append(((System.currentTimeMillis() - _lastContact) / 1000))
				.append(" sec, Num msgs: ").append(_count);
		return sb.toString();
	}

	public void setVerbose(boolean on) {
		_verbose = on;
	}

	/**
	 * register for self removal
	 * 
	 * @param _sessions
	 */
	void registerBack(Sessions sessions) {
		this._sessions = sessions;
	}

	/**
	 * stops session on next _timeout cycle
	 */
	public void stopSession() {
		_forever = false;

		if (_connection != null) {
			try {
				_sessions.remove(this);
				_connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		_connection = null;
	}

	public long getSessionId() {
		return _id;
	}

	public long getLastContact() {
		return _lastContact;
	}

	public void setTimeOut(int v) {
		_timeout = v;
	}

	public void setSessionName(String n) {
		_name = n;
	}

	public String getSessionName() {
		return _name;
	}

	public long getCount() {
		return _count;
	}

	/**
	 * process incoming data
	 */
	public void run() {
		if (_verbose)
			System.out.println("Session " + _id + " started");

		try {
			_connection.setSoTimeout(_timeout);
			_inSock = new BufferedInputStream(_connection.getInputStream());

			byte[] raw = new byte[2048];
			_msgBuilder = new BasicBuilder();
			_msgBuilder.setVerbose(_verbose);
			while (_forever) {
				try {
					int len = _inSock.read(raw);
					if (len == 0)
						continue;
					else if (len == -1)
						break;

					List<Message> list = _msgBuilder.decode(new String(raw, 0, len).getBytes());
					for (Message msg : list) {
						if (msg.getType() == MessageType.createTopic) {
							 String s = _masterService.create_topic(msg.getPayload());
							 respondToCreateTopic(msg,s);
						} else if (msg.getType() == MessageType.subscribeTopic) {
							 String s = _masterService.substribe_topic(msg.getTopicMessage().getTopic_name(),msg.getSource());
							 respondToCreateTopic(msg,s);
						}else if (msg.getType() == MessageType.sendMessage) {
							TopicMessage tm = new TopicMessage();
							tm.setMessageId(msg.getMid());
							tm.setTopic_name(msg.getSource());
							tm.setMessageString(msg.getPayload());
							 String s = _masterService.write_message(tm);
							 msg.setPayload(s);
							respondToProduceMessage(msg,s);
						}  else {
							// TODO unknown type
						}
					}

					updateLastMsgReceived();

				} catch (InterruptedIOException ioe) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (_verbose) {
					System.out.println("Session " + (_name == null ? "" : _name) + " [" + _id + "] exiting");
					System.out.flush();
				}

				stopSession();
			} catch (Exception re) {
				re.printStackTrace();
			}
		}
	}

	/**
	 * respond to a received message
	 * 
	 * @param msg
	 */
	private void respondToMsg(Message msg) {
		if (_verbose)
			System.out.println("--> responding to a msg: " + msg);
		msg.setStatus("200");
		try {
			send(msg.getDestination(),msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ackResponse(msg, ""+new Date(System.currentTimeMillis()));

		
	}
	
	/* Message Received */
	private void sendList(Message msg) {
		if (_verbose)
			System.out.println("--> responding to a msg: " + msg);
		msg.setStatus("200");
		ArrayList<String> ar = new ArrayList<String>();
		for(SessionHandler sh:_sessions.getConnections()) {
			ar.add(""+sh._id);
		}
		msg.setPeers(ar);
		ackResponse(msg,"Success");

		
	}
	

	/**
	 * TODO what is the join response?
	 * 
	 * @param msg
	 */
	private void respondToCreateTopic(Message msg,String s) {
		if (_verbose)
			System.out.println("--> responding to join: " + msg);
		msg.setStatus("200");
		msg.setSource(""+this.getId());
		msg.setPayload(s);
		ackResponse(msg, s);
	}
	
	private void respondToProduceMessage(Message msg,String s) {
		if (_verbose)
			System.out.println("--> responding to join: " + msg);
		msg.setStatus("200");
		msg.setSource(""+this.getId());
		ackResponse(msg, s);
	}

	/**
	 * respond to a message received
	 * 
	 * @param msg
	 */
	private void ackResponse(Message msg, String body) {
		try {
			var builder = new BasicBuilder();
			byte[] raw = builder.encode(msg.getType(), msg.getMid(), msg.getSource(), msg.getPayload(),msg.getDestination(), msg.getReceived()).getBytes();
			_connection.getOutputStream().write(raw);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void ackResponse(Message msg, List<String> body) {
		try {
			String response = _json.encode("Msg Type: "+msg.getType()+" Msg Id: "+msg.getMid()+"Msg Status:"+msg.getStatus()+" server: "+ body+" Status: "+ msg.getReceived());
			_connection.getOutputStream().write(response.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * record when last message was received - used for timing out of
	 * channel/socket
	 */
	private void updateLastMsgReceived() {
		this._lastContact = System.currentTimeMillis();
		this._count++;
	}

	/**
	 * send message to all connections
	 * 
	 * @param msg
	 *            String
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private synchronized void send(Message msg) throws Exception {
		for (SessionHandler sh : _sessions.getConnections()) {
			if((""+sh.getId()).equalsIgnoreCase(msg.getSource())) {
			var builder = new BasicBuilder();
			byte[] raw = builder
					.encode(msg.getType(), msg.getMid(), msg.getSource(), msg.getPayload(),msg.getDestination(), msg.getReceived())
					.getBytes();
			sh._connection.getOutputStream().write(raw);
			sh._connection.getOutputStream().flush();
			}
		}
	}

	/**
	 * send message to a _connection
	 * 
	 * @param msg
	 *            String
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private synchronized void send(String to, Message msg) throws Exception {
		try {
		for (SessionHandler sh : _sessions.getConnections()) {
			if (sh.getSessionId() == Integer.valueOf(to)) {
				var builder = new BasicBuilder();
				byte[] raw = builder
						.encode(msg.getType(), msg.getMid(), msg.getSource(), msg.getPayload(),msg.getDestination(), msg.getReceived())
						.getBytes();
				sh._connection.getOutputStream().write(raw);
				sh._connection.getOutputStream().flush();
				break;
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

} // class SessionHandler