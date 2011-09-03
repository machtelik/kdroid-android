package org.kde.kdroid.net;

import java.io.IOException;
import java.net.Socket;

public class TCPClientPort extends TCPPort {

	private Socket clientSocket = null;

	public TCPClientPort(int Port) {
		port = Port + 1;
	}

	@Override
	public void setPort(int port) {
		this.port = port + 1;
		if (clientSocket != null) {
			try {
				clientSocket.close();
				clientSocket = null;
				out = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean send(Packet packet) {
		if (connect()) {
			boolean status = super.send(packet);
			disconnect();
			return status;
		}
		return false;
	}

	private boolean connect() {
		if (address != null) {
			try {
				clientSocket = new Socket(address, port);
				out = clientSocket.getOutputStream();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private void disconnect() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		clientSocket = null;
		out = null;
	}

}
