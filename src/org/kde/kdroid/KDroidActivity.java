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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

public class KDroidActivity extends Activity {
	private Button saveButton;
	private EditText portEdit;
	private Button defaultButton;
	private CheckBox serviceCheckBox;
	private ToggleButton serviceToggleButton;
	
	private KDroidServiceApi api;
	private boolean isBound = false;

	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		  @Override
		  public void onServiceConnected(ComponentName name, IBinder service) {
		    Log.i("KDroid", "Service connection established");
		 
		    api = KDroidServiceApi.Stub.asInterface(service);
		    isBound=true;
		  }
		 
		  @Override
		  public void onServiceDisconnected(ComponentName name) {
		    Log.i("KDroid", "Service connection closed");
		    isBound=false;
		  }
		};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SharedPreferences settings = getSharedPreferences("KDroidSettings", 0);
		
		boolean startService = settings.getBoolean("serviceStartet", true);
		
		if(startService) {

			Intent intent = new Intent(KDroidService.class.getName());

			startService(intent);

			bindService(intent, serviceConnection, 0);
		}
		
		serviceCheckBox = (CheckBox) findViewById(R.id.serviceCheckBox);
		
		serviceToggleButton = (ToggleButton) findViewById(R.id.serviceToggleButton);
		serviceToggleButton.setChecked(startService);
		serviceToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				saveSettings();
				if(isChecked && !isBound) {
					Intent intent = new Intent(KDroidService.class.getName());

					startService(intent);

					bindService(intent, serviceConnection, 0);
				}
				else if(!isChecked) {
					if(isBound) {
						unbindService(serviceConnection);
					}
					Intent intent = new Intent(KDroidService.class.getName());

					stopService(intent);
					
					isBound=false;
				}
			}

		});

		portEdit = (EditText) findViewById(R.id.portEdit);
		portEdit.setText(Integer.toString(settings.getInt("port", 48564)));
		
		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveSettings();
				if(isBound) {
					int port = Integer.parseInt(portEdit.getText().toString());
					try {
						api.setPort(port);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}

		});

		defaultButton = (Button) findViewById(R.id.defaultButton);
		defaultButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				portEdit.setText("48564");
				saveSettings();
				if(isBound) {
					try {
						api.setPort(48564);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}

		});

		Log.d("KDroid", "KDroid started");

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(isBound) {
			unbindService(serviceConnection);
		}
	}
    
    private void saveSettings() {
        SharedPreferences settings = getSharedPreferences("KDroidSettings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("port",Integer.parseInt(portEdit.getText().toString()));
        editor.putBoolean("serviceOnBoot", serviceCheckBox.isChecked());
        editor.putBoolean("serviceStartet", serviceToggleButton.isChecked());
        editor.commit();
    }


}