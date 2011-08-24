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

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.kde.kdroid.contact.Contact;
import org.kde.kdroid.sms.SMSMessage;

public class Packet {

	private Vector<String> arguments = new Vector<String>();
	private final String separator = new String(new char[] { 30 }); // argument
																	// seperator
																	// - asci 30
																	// = record
																	// separator
	private String type;

	public Packet(SMSMessage message) {
		type = "SMS";
		addArgument(message.Id);
		addArgument(message.ThreadId);
		addArgument(message.Address);
		addArgument(message.PersonId);
		addArgument(message.Body);
		addArgument(message.Time);
		addArgument(message.Type);
	}

	public Packet(Contact contact) {
		type = "Contact";
		addArgument(contact.Id);
		addArgument(contact.Name);
		addArgument(contact.Address);
		addArgument(contact.ThreadId);
	}

	public Packet(String type) {
		this.type = type;
	}

	SMSMessage toSMSMessage() {
		SMSMessage message = new SMSMessage();
		message.Id = arguments.elementAt(0);
		message.ThreadId = arguments.elementAt(1);
		message.Address = arguments.elementAt(2);
		message.PersonId = arguments.elementAt(3);
		message.Body = arguments.elementAt(4);
		message.Time = arguments.elementAt(5);
		message.Type = arguments.elementAt(6);

		return message;
	}

	public void addArgument(String argument) {
		arguments.add(argument);
	}

	Vector<String> getArguments() {
		return arguments;
	}

	String getType() {
		return type;
	}

	public Packet(byte[] data) {
		String dat = new String(data);
		String[] list = dat.split(separator);
		type = list[0];
		for (int i = 1; i < list.length - 1; ++i) {
			addArgument(list[i]);
		}
	}

	public byte[] toByteArray() {
		String data = new String();
		data += type;
		data += separator;
		for (int i = 0; i < arguments.size(); ++i) {
			data += arguments.elementAt(i);
			data += separator;
		}
		try {
			return data.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
