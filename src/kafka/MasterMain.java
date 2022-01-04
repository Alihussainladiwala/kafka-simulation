package kafka;

import java.util.Properties;

import kafka.comm.core.BasicSocketServer;

public class MasterMain {


	public static void main(String[] args) {
		var p = new Properties();
		var server = new BasicSocketServer(p);
		server.start();
	}
}
