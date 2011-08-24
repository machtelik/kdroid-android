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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class KDroidActivity extends Activity {
	private Button saveButton;
	private EditText portEdit;
	private Button defaultButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		startService(new Intent(KDroidService.class.getName()));

		portEdit = (EditText) findViewById(R.id.portEdit);

		saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}

		});

		defaultButton = (Button) findViewById(R.id.defaultButton);
		defaultButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				portEdit.setText("48564");
			}

		});

		Log.d("KDroid", "KDroid started");

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

}