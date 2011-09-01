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

import org.kde.kdroid.contact.ContactHandler;
import org.kde.kdroid.net.Dispatcher;
import org.kde.kdroid.net.TCPClientPort;
import org.kde.kdroid.net.TCPServerPort;
import org.kde.kdroid.sms.SMSHandler;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class KDroidService extends Service {

	private TCPServerPort tcpServerPort;
	private TCPClientPort tcpClientPort;
	private SMSHandler sms;
	private ContactHandler contact;
	private Dispatcher dispatcher;
	
	private KDroidServiceApi.Stub apiEndpoint = new KDroidServiceApi.Stub() {

		@Override
		public void setPort(int Port) throws RemoteException {
			tcpServerPort.setPort(Port);
			tcpClientPort.setPort(Port);
			tcpServerPort.start();
		}
		
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("KDroid", "Service created");
		SharedPreferences settings = getSharedPreferences("KDroidSettings", 0);
		int Port = settings.getInt("port", 48564);
		tcpServerPort = new TCPServerPort(Port);
		tcpClientPort = new TCPClientPort(Port);
		sms = new SMSHandler(getBaseContext(), tcpServerPort,tcpClientPort);
		contact = new ContactHandler(getBaseContext(), tcpServerPort,sms);
		dispatcher = new Dispatcher(sms, contact, tcpServerPort);
		tcpServerPort.setDispatcher(dispatcher);
		tcpServerPort.start();
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("KDroid", "Service received start id " + startId + ": " + intent);
		tcpServerPort.start();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		sms.unregisterReciever();
		tcpServerPort.stop();
		Log.d("KDroid", "Service destroyed");
	}

	@Override
	public IBinder onBind(Intent intent) {
		  if (KDroidService.class.getName().equals(intent.getAction())) {
			    Log.d("KDroid", "Bound by intent " + intent);
			    return apiEndpoint;
			  } else {
			    return null;
			  }
	}

}
