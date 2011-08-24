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

package org.kde.kdroid.sms;

import org.kde.kdroid.net.Packet;
import org.kde.kdroid.net.Port;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSHandler {

	ContentResolver cr;
	Context context;

	Port port;

	public SMSHandler(Context context, Port port) {
		this.port = port;
		this.context = context;
		cr = context.getContentResolver();
	}

	public void sendSMS(SMSMessage message) {
		Log.d("KDroid", "Sending SMS");
		String address = message.Address;
		String body = message.Body;

		// Send the Message
		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(
				context, SMSHandler.class), 0);
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(address, null, body, pi, null);

		// Save the Message in the Database
		ContentValues values = new ContentValues();
		values.put("address", address);
		values.put("date", message.Time);
		values.put("read", 1);
		values.put("status", -1);
		values.put("type", 2);
		values.put("body", body);
		cr.insert(Uri.parse("content://sms"), values);

		Packet packet = new Packet("Status");
		packet.addArgument("SMSSend");
		port.send(packet);
	}

	public void returnAllMessages() {
		Log.d("KDroid", "Returning All SMS");
		returnMessagesInbox();
		returnMessagesOutbox();
	}

	public void returnSMS(SMSMessage message) {
		Packet packet = new Packet(message);
		try {
			port.send(packet);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

	public void returnMessagesInbox() {

		final Cursor c = cr.query(Uri.parse("content://sms/inbox"), null, null,
				null, null);

		while (c.moveToNext()) {

			SMSMessage message = new SMSMessage();

			int ID = c.getInt(c.getColumnIndex("_id"));
			int threadID = c.getInt(c.getColumnIndex("thread_id"));
			long then = c.getLong(c.getColumnIndex("date"));
			int person = c.getInt(c.getColumnIndex("person"));
			String address = c.getString(c.getColumnIndex("address"));
			String body = c.getString(c.getColumnIndex("body"));

			message.Id = Integer.toString(ID);
			message.ThreadId = Integer.toString(threadID);
			message.PersonId = Integer.toString(person);
			message.Body = body;
			message.Address = PhoneNumberUtils.formatNumber(address);
			message.Time = String.valueOf(then);
			message.Type = "Incoming";

			returnSMS(message);

		}

		c.close();
	}

	public void returnMessagesOutbox() {

		final Cursor c = cr.query(Uri.parse("content://sms/sent"), null, null,
				null, null);

		while (c.moveToNext()) {

			SMSMessage message = new SMSMessage();

			int ID = c.getInt(c.getColumnIndex("_id"));
			int threadID = c.getInt(c.getColumnIndex("thread_id"));
			long then = c.getLong(c.getColumnIndex("date"));
			String address = c.getString(c.getColumnIndex("address"));
			String body = c.getString(c.getColumnIndex("body"));

			message.Id = Integer.toString(ID);
			message.ThreadId = Integer.toString(threadID);
			message.Body = body;
			message.Address = address;
			message.Time = String.valueOf(then);
			message.Type = "Outgoing";
			message.PersonId = "-1";

			returnSMS(message);

		}

		c.close();

	}

}
