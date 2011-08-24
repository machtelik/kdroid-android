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

package org.kde.kdroid;

import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import org.kde.kdroid.contact.ContactHandler;
import org.kde.kdroid.net.Dispatcher;
import org.kde.kdroid.net.Port;
import org.kde.kdroid.sms.SMSHandler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class KDroidService extends Service {

	private Port port;
	private SMSHandler sms;
	private ContactHandler contact;
	private Dispatcher dispatcher;

	private Timer timer;

	private TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			port.recive();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("KDroid", "Service created");
		try {
			port = new Port();
			sms = new SMSHandler(getBaseContext(), port);
			contact = new ContactHandler(getBaseContext(), port);
			dispatcher = new Dispatcher(sms, contact, port);
			port.setDispatcher(dispatcher);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		timer = new Timer("ReceiveTimer");
		timer.schedule(updateTask, 1000L, 500L);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("KDroid", "Service received start id " + startId + ": " + intent);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d("KDroid", "Service destroyed");
	}

	public void setPort(int Port) {
		port.setPort(Port);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}