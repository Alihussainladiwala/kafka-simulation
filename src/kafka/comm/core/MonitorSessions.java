package kafka.comm.core;

/**
 * 
 * @author gash
 * 
 */
public class MonitorSessions extends Thread {
	private boolean _forever = true;
	private long _interval;
	private long _idleTime;
	private Sessions _sessions;

	/**
	 * create a new monitor
	 * 
	 * @param interval
	 *            long how often to check
	 * @param idleness
	 *            long what is considered idle
	 */
	public MonitorSessions(Sessions sessions, long interval, long idleness) {
		this._sessions = sessions;
		this._interval = interval;
		this._idleTime = idleness;
	}

	/**
	 * stop monitoring on the next _interval
	 */
	public void stopMonitoring() {
		_forever = false;
	}

	/**
	 * ran in the thread to monitor for idle threads
	 */
	public void run() {
		while (_forever) {
			try {
				long idle = System.currentTimeMillis() - _idleTime;
				Thread.sleep(_interval);
				if (!_forever) {
					break;
				}

				/**
				 * TODO what should we do in addition to checking for idle
				 * connections?
				 */
				for (SessionHandler sh : _sessions.getConnections()) {
					if (sh.getLastContact() < idle) {
						System.out.println("MonitorSessions stopping session " + sh.getSessionId());

						sh.stopSession();
						_sessions.remove(sh);
					}else {
						//sh.run();
					}
				}
			} catch (Exception e) {
				break;
			}
		}
	}
} // class MonitorSessions
