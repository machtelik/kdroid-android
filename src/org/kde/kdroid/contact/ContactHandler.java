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

package org.kde.kdroid.contact;

import org.kde.kdroid.net.Packet;
import org.kde.kdroid.net.Port;
import org.kde.kdroid.sms.SMSHandler;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

public class ContactHandler {

	ContentResolver cr;

	Port port;
	SMSHandler sms;

	public ContactHandler(Context context, Port port, SMSHandler sms) {
		this.port = port;
		cr = context.getContentResolver();
		this.sms=sms;
	}

	public void returnAllContacts() {
		Log.d("KDroid", "Returning All Contacts");

		Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] { Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER },
				null, null, null);

		while (c.moveToNext()) {

			Contact contact = new Contact();

			int ID = c.getInt(0);
			String name = c.getString(1);
			String address = c.getString(2);

			contact.Id = Integer.toString(ID);
			contact.Name = name;
			contact.Address = address;
			contact.ThreadId = Integer.toString(sms.getSMSThreadId(address));

			returnContact(contact);

		}

		c.close();

	}

	public void returnUnknownContactNumbers() {
		Log.d("KDroid", "Returning All Unknown Contacts");

		final Cursor c = cr.query(Uri.parse("content://sms/inbox"),
				new String[] { "DISTINCT address", "person", "thread_id" },
				null, null, null);

		while (c.moveToNext()) {

			int person = c.getInt(c.getColumnIndex("person"));

			if (person == 0) {

				Contact contact = new Contact();

				String address = c.getString(c.getColumnIndex("address"));
				int threadId = c.getInt(c.getColumnIndex("thread_id"));

				contact.Id = "0";
				contact.Name = "Unknown";
				contact.Address = address;
				contact.ThreadId = Integer.toString(threadId);

				returnContact(contact);

			}

		}

		c.close();

	}

	public void returnContact(Contact contact) {
		Packet packet = new Packet(contact);
		try {
			port.send(packet);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

}
