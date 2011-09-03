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
import java.net.Socket;

public class TCPClientPort extends TCPPort {

	private Socket clientSocket = null;

	public TCPClientPort(int Port) {
		port = Port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
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
		if (address != null && !address.isLoopbackAddress()) {
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
