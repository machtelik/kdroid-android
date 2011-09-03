package org.kde.kdroid.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;

public abstract class TCPPort {

	protected OutputStream out = null;
	protected final int packetSeparator = 31;
	protected Dispatcher dispatcher = null;
	protected int port = 48564;
	protected static InetAddress address = null;

	public abstract void setPort(int port);

	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public boolean send(Packet packet) {
		if (out != null) {
			try {
				byte[] bytes = packet.toByteArray();
				out.write(bytes, 0, bytes.length);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
