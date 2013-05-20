/*
    Open Manager, an open source file manager for the Android system
    Copyright (C) 2009, 2010, 2011  Joe Berria <nexesdevelopment@gmail.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zhan_dui.dictionary.filemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.zhan_dui.dictionary.R;

public class FileManagerSettings extends Activity {
	private boolean mHiddenChanged = false;
	private boolean mThumbnailChanged = false;
	private boolean mSortChanged = false;

	private boolean hidden_state;
	private boolean thumbnail_state;
	private int sort_state;
	private Intent is = new Intent();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_manager_settings);

		Intent i = getIntent();
		hidden_state = i.getExtras().getBoolean("HIDDEN");
		thumbnail_state = i.getExtras().getBoolean("THUMBNAIL");
		sort_state = i.getExtras().getInt("SORT");

		final CheckBox hidden_bx = (CheckBox) findViewById(R.id.setting_hidden_box);
		final ImageButton sort_bt = (ImageButton) findViewById(R.id.settings_sort_button);

		hidden_bx.setChecked(hidden_state);

		hidden_bx
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						hidden_state = isChecked;

						is.putExtra("HIDDEN", hidden_state);
						mHiddenChanged = true;
					}
				});

		sort_bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						FileManagerSettings.this);
				CharSequence[] options = {getText(R.string.sort_none),
						getText(R.string.sort_alphabetical),
						getText(R.string.sort_type),
						getText(R.string.sort_size)};

				builder.setTitle(R.string.sort_by);
				builder.setIcon(R.drawable.filter);
				builder.setSingleChoiceItems(options, sort_state,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int index) {
								switch (index) {
									case 0 :
										sort_state = 0;
										mSortChanged = true;
										is.putExtra("SORT", sort_state);
										break;

									case 1 :
										sort_state = 1;
										mSortChanged = true;
										is.putExtra("SORT", sort_state);
										break;

									case 2 :
										sort_state = 2;
										mSortChanged = true;
										is.putExtra("SORT", sort_state);
										break;

									case 3 :
										sort_state = 3;
										mSortChanged = true;
										is.putExtra("SORT", sort_state);
										break;
								}
							}
						});

				builder.create().show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mHiddenChanged)
			is.putExtra("HIDDEN", hidden_state);

		if (!mSortChanged)
			is.putExtra("SORT", sort_state);

		setResult(RESULT_CANCELED, is);
	}
}
