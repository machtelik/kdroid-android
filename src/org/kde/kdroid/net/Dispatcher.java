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

import org.kde.kdroid.contact.ContactHandler;
import org.kde.kdroid.net.Packet.Type;
import org.kde.kdroid.sms.SMSHandler;
import org.kde.kdroid.sms.SMSMessage;

import android.util.Log;

public class Dispatcher {

	private SMSHandler sms;
	private ContactHandler contact;
	private TCPServerPort tcpServerPort;
	private TCPClientPort tcpClientPort;

	public Dispatcher(SMSHandler sms, ContactHandler contact,
			TCPServerPort tcpServerPort, TCPClientPort tcpClientPort) {
		this.sms = sms;
		this.contact = contact;
		this.tcpServerPort = tcpServerPort;
		this.tcpClientPort = tcpClientPort;
	}

	public void dispatch(Packet packet) {
		Log.d("KDroid", "Dispatching " + packet.getType());
		if (packet.getType().compareTo("SMS") == 0) {
			SMSMessage message = packet.toSMSMessage();
			if (message.Type.compareTo("Send") == 0) {
				sms.sendSMS(message);
				endServerConnection();
				return;
			}
		}
		if (packet.getType().compareTo("Request") == 0) {
			if (packet.getArguments().elementAt(0).compareTo("getAll") == 0) {
				Packet p = new Packet(Type.Status);
				p.addArgument("AckGetAll");
				tcpServerPort.send(p);

				contact.returnAllContacts();
				sms.returnAllMessages();

				p = new Packet(Type.Status);
				p.addArgument("DoneGetAll");
				tcpServerPort.send(p);

				endServerConnection();
				return;
			}
		}

		if (packet.getType().compareTo("Status") == 0) {
			if (packet.getArguments().elementAt(0).compareTo("connectionTest") == 0) {
				endServerConnection();
				Log.d("KDroid", "Connection Test: "
						+ packet.getArguments().elementAt(1));
				Packet p = new Packet(Type.Status);
				p.addArgument("connectionSuccessful");
				p.addArgument(packet.getArguments().elementAt(1));
				tcpClientPort.send(p);
				return;
			}
		}
		Log.d("KDroid", "Unknown Packet");
	}

	private void endServerConnection() {
		Packet p = new Packet(Type.Status);
		p.addArgument("end");
		tcpServerPort.send(p);
		Log.d("KDroid", "End");
	}

}
