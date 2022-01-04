package kafka.comm.core;

import java.net.ServerSocket;
import java.util.Properties;

/**
 * server to manage incoming clients. Default port is 2100.
 * 
 * @author gash
 * 
 */
public class BasicSocketServer {
	private Properties _setup;
	private ServerSocket _socket;
	private long _idCounter = 1;
	private boolean _forever = true;
	private Sessions _connections;
	private MonitorSessions _monitor;

	private boolean _verbose = true;

	private static final int sDefaultPort = 2100;
	private static final long sDefaultFreq = 30 * 1000;
	private static final long sDefaultIdle = 60 * 1000 * 60;

	public BasicSocketServer() {
		this._setup = new Properties();
		this._setup.setProperty(Settings.PropertyPort, String.valueOf(sDefaultPort));
		this._setup.setProperty(Settings.PropertyTimeout, String.valueOf(sDefaultIdle));
		this._setup.setProperty(Settings.PropertyFreq, String.valueOf(sDefaultFreq));

		this._connections = new Sessions();
	}

	/**
	 * construct a new server listening on the specified port
	 */
	public BasicSocketServer(Properties setup) {
		this._setup = setup;
		this._connections = new Sessions();
	}

	public void setVerbose(boolean on) {
		_verbose = on;
	}

	/**
	 * start monitoring _socket for new _connections
	 */
	public void start() {
		if (_setup == null)
			throw new RuntimeException("Missing configuration properties");

		try {
			var port = Integer.parseInt(_setup.getProperty(Settings.PropertyPort, String.valueOf(sDefaultPort)));
			_socket = new ServerSocket(port);

			// how frequent do we monitor and when to remove idle sessions
			var idleout = Long.parseLong(_setup.getProperty(Settings.PropertyTimeout,String.valueOf(sDefaultIdle)));
			if (idleout < sDefaultIdle)
				idleout = sDefaultIdle;

			var freqCheck = Long.parseLong(_setup.getProperty(Settings.PropertyFreq,String.valueOf(sDefaultFreq)));
			if (freqCheck < sDefaultFreq/2)
				freqCheck = sDefaultFreq/2;

			if (_verbose)
				System.out.println("Server Host: " + _socket.getInetAddress().getHostAddress() + ", port: " + port
						+ ", idle: " + idleout + ", check: " + freqCheck);

			_monitor = new MonitorSessions(this._connections, freqCheck, idleout);

			while (_forever) {
				var s = _socket.accept();
				if (!_forever) {
					break;
				}

				if (_verbose) {
					System.out.println("--> server got a client connection");
					System.out.flush();
				}

				var sh = new SessionHandler(s, _idCounter++);
				_connections.add(sh);
				sh.setVerbose(_verbose);
				sh.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void stopSessions() {
		if (_monitor != null)
			_monitor.stopMonitoring();

		for (SessionHandler sh : _connections.getConnections()) {
			sh.stopSession();
		}

		_connections = null;
		_forever = false;
	}
}
