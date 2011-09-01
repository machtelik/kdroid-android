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
	
	public Dispatcher(SMSHandler sms,ContactHandler contact, TCPServerPort tcpServerPort) {
		this.sms=sms;
		this.contact=contact;
		this.tcpServerPort = tcpServerPort;
	}
	
	public void dispatch(Packet packet) {
		Log.d("KDroid", "Dispatching "+packet.getType());
		if(packet.getType().compareTo("SMS")==0) {
			SMSMessage message = packet.toSMSMessage();
			sms.sendSMS(message);
		}
		if(packet.getType().compareTo("Request")==0) {
			if(packet.getArguments().elementAt(0).compareTo("getAll")==0) {
				Packet p = new Packet(Type.Status);
				p.addArgument("AckGetAll");
				tcpServerPort.send(p);
				
				contact.returnAllContacts();
				sms.returnAllMessages();
				
				p = new Packet(Type.Status);
				p.addArgument("DoneGetAll");
				tcpServerPort.send(p);
			}
		}
		
		Packet p = new Packet(Type.Status);
		p.addArgument("end");
		tcpServerPort.send(p);
		Log.d("KDroid","End");
	}
	
}
