package kafka.comm.core;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class Sessions {
	private ConcurrentMap<SessionHandler, SessionHandler> _connections = new ConcurrentHashMap<SessionHandler, SessionHandler>();

	public Set<SessionHandler> getConnections() {
		return _connections.keySet();
	}

	public void add(SessionHandler connection) {
		if (connection == null)
			return;
		connection.registerBack(this);
		this._connections.put(connection, connection);
	}

	public void remove(SessionHandler session) {
		this._connections.remove(session);
	}
}
