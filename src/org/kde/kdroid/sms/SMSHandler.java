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

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSHandler {

	ContentResolver cr;
	Context context;

	Port port;
	
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String OUT = "Outgoing";
	private static final String IN = "Incoming";
	
	private BroadcastReceiver smsReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(SMS_RECEIVED)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[])bundle.get("pdus");
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        SMSMessage message = new SMSMessage();
                        message.Body = messages[i].getDisplayMessageBody();
                        message.Address = messages[i].getDisplayOriginatingAddress();
                        message.Time = Long.toString(messages[i].getTimestampMillis());
                        message.Type=IN;
                        port.send(new Packet(message));
                    }
                    Log.d("KDroid", "Message recieved");
                }
            }

		}
	};

	public SMSHandler(Context Context, Port Port) {
		this.port = Port;
		this.context = Context;
		cr = context.getContentResolver();
		context.registerReceiver(smsReciever, new IntentFilter(SMS_RECEIVED));
	}
	
	public void unregisterReciever() {
		context.unregisterReceiver(smsReciever);
	}

	public void sendSMS(SMSMessage message) {
		Log.d("KDroid", "Sending SMS");
		final String address = message.Address;
		final String body = message.Body;
		final String time = message.Time;

		PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(
				"SMS_SENT"), 0);

		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					ContentValues values = new ContentValues();
					values.put("address", address);
					values.put("date", time);
					values.put("read", 1);
					values.put("status", -1);
					values.put("type", 2);
					values.put("body", body);
					cr.insert(Uri.parse("content://sms"), values);
					Packet packet = new Packet("Status");
					packet.addArgument("SMSSend");
					port.send(packet);
					SMSMessage message = new SMSMessage();
                    message.Body = body;
                    message.Address = address;
                    message.Time = time;
                    message.Type=OUT;
                    port.send(new Packet(message));
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:

					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:

					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:

					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:

					break;
				}
				context.unregisterReceiver(this);
			}
		}, new IntentFilter("SMS_SENT"));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(address, null, body, pi, null);
	}

	public void returnAllMessages() {
		Log.d("KDroid", "Returning All SMS");
		final Cursor c = cr.query(Uri.parse("content://sms"), null, null,
				null, null);

		while (c.moveToNext()) {

			SMSMessage message = new SMSMessage();

			int ID = c.getInt(c.getColumnIndex("_id"));
			long then = c.getLong(c.getColumnIndex("date"));
			String address = c.getString(c.getColumnIndex("address"));
			String body = c.getString(c.getColumnIndex("body"));
			int type  = c.getInt(c.getColumnIndex("type"));

			message.Id = Integer.toString(ID);
			message.Body = body;
			message.Address = address;
			message.Time = String.valueOf(then);
			if(type==1) {
				message.Type = IN;
			} else {
				message.Type = OUT;
			}

			returnSMS(message);

		}

		c.close();
	}
	
	
	public void returnSMS(SMSMessage message) {
		Packet packet = new Packet(message);
		try {
			port.send(packet);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

}
