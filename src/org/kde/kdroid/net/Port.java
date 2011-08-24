/***************************************************************************
 *   Copyright (C) 2011 by Mike Achtelik                                   *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA .        *
 ***************************************************************************/

package org.kde.kdroid.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Port {

	private DatagramSocket socket;
	private int port = 48564;
	private InetAddress address;
	private Dispatcher dispatcher = null;

	protected void finalize() throws Throwable {
		super.finalize();
		socket.close();
	}

	public void close() {
		socket.close();
	}

	public void send(Packet data) {
		byte[] byteArray = data.toByteArray();
		DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length,
				address, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Port() throws SocketException {
		socket = new DatagramSocket(port);
	}

	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public void setPort(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void recive() {
		byte[] buffer = new byte[1536];
		DatagramPacket packet = new DatagramPacket(buffer, 1536);
		try {
			socket.receive(packet);
			address = packet.getAddress();
			if (dispatcher != null) {
				dispatcher.dispatch(new Packet(packet.getData()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}