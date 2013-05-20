package com.zhan_dui.dictionary.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.SimpleDragSortCursorAdapter;
import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.utils.Constants;

public class SetActivity extends Activity {
	private Context context;
	private DragSortListView dragSortListView;
	private SimpleDragSortCursorAdapter simpleDragSortCursorAdapter;

	private SharedPreferences sharedPreferences;
	private Button setSmallSizeButton, setMediumSizeButton, setLargeSizeButton,
			changeHandModeBtn;
	private TextView currentHandModeTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		context = this;
		sharedPreferences = context.getSharedPreferences(Constants.PREF_FIRST,
				Context.MODE_PRIVATE);
		String currentHandMode = sharedPreferences.getString("openMode",
				"defaultMode");

		setSmallSizeButton = (Button) findViewById(R.id.setSmall);
		setMediumSizeButton = (Button) findViewById(R.id.setMedium);
		setLargeSizeButton = (Button) findViewById(R.id.setLarge);
		currentHandModeTextView = (TextView) findViewById(R.id.currentHandMode);

		if (currentHandMode.equalsIgnoreCase("leftMode")) {
			currentHandModeTextView.setText(R.string.settingLeftMode);
		} else if (currentHandMode.equalsIgnoreCase("rightMode")) {
			currentHandModeTextView.setText(R.string.settingRightMode);
		} else {
			currentHandModeTextView.setText(R.string.settingDefaultMode);
		}

		setSmallSizeButton.setOnClickListener(setSizeListener);
		setMediumSizeButton.setOnClickListener(setSizeListener);
		setLargeSizeButton.setOnClickListener(setSizeListener);

		dragSortListView = (DragSortListView) findViewById(R.id.drag_sort_list);
		changeHandModeBtn = (Button) findViewById(R.id.changeMode);
		changeHandModeBtn.setOnClickListener(changeHandModeBtnListener);

		DictionaryDB dictionaryDB = new DictionaryDB(context,
				DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
		Cursor cursor = sqLiteDatabase
				.rawQuery(
						"select * from dictionary_list order by dictionary_order",
						null);
		String[] from = {"dictionary_name"};
		int[] to = {R.id.dictionary_name};
		simpleDragSortCursorAdapter = new SimpleDragSortCursorAdapter(context,
				R.layout.drag_and_drop_item, cursor, from, to, 0);

		DragSortController dragSortController = new DragSortController(
				dragSortListView);
		dragSortController.setBackgroundColor(getResources().getColor(
				R.color.floatviewcolor));
		dragSortController.setDragHandleId(R.id.drag_image);

		dragSortListView.setFloatViewManager(dragSortController);
		dragSortListView.setAdapter(simpleDragSortCursorAdapter);
		dragSortListView.setDropListener(dropListener);
		dragSortListView.setOnTouchListener(dragSortController);

		sqLiteDatabase.close();
	}
	/**
	 * ÅÅÐò¼àÌýÆ÷
	 */
	private DropListener dropListener = new DropListener() {

		@Override
		public void drop(int from, int to) {

			DictionaryDB dictionaryDB = new DictionaryDB(context,
					DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
			SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
			sqLiteDatabase
					.execSQL("update dictionary_list set dictionary_order='10000000' where dictionary_order='"
							+ from + "'");
			if (from > to) {
				sqLiteDatabase
						.execSQL("update dictionary_list set dictionary_order=dictionary_order+1 where dictionary_order>='"
								+ to + "' and dictionary_order<'" + from + "'");
			} else if (from < to) {
				sqLiteDatabase
						.execSQL("update dictionary_list set dictionary_order=dictionary_order-1 where dictionary_order>'"
								+ from + "' and dictionary_order<='" + to + "'");
			}
			sqLiteDatabase
					.execSQL("update dictionary_list set dictionary_order='"
							+ to + "' where dictionary_order='10000000'");

			Cursor cursor2 = sqLiteDatabase.rawQuery(
					"select * from dictionary_list order by dictionary_order",
					null);
			String[] from2 = {"dictionary_name"};
			int[] to2 = {R.id.dictionary_name};
			simpleDragSortCursorAdapter = new SimpleDragSortCursorAdapter(
					context, R.layout.drag_and_drop_item, cursor2, from2, to2,
					0);
			dragSortListView.setAdapter(simpleDragSortCursorAdapter);
			sqLiteDatabase.close();
		}
	};

	/**
	 * ÊÖ³ÖÄ£Ê½ToggleButton¼àÌýÆ÷
	 */
	private OnClickListener changeHandModeBtnListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View contentView = layoutInflater.inflate(
					R.layout.select_mode, null);
			String mode = sharedPreferences
					.getString("openMode", "defaultMode");
			final RadioGroup radioGroup = (RadioGroup) contentView
					.findViewById(R.id.changeModeRadioGroup);
			int chechedId = 0;
			if (mode.equalsIgnoreCase("leftMode")) {
				chechedId = R.id.left_mode;
			} else if (mode.equalsIgnoreCase("rightMode")) {
				chechedId = R.id.right_mode;
			} else {
				chechedId = R.id.default_mode;
			}
			RadioButton radioButton = (RadioButton) contentView
					.findViewById(chechedId);
			radioButton.setChecked(true);

			DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					int checkedId = radioGroup.getCheckedRadioButtonId();
					String mode = "defaultMode";
					switch (checkedId) {
						case R.id.left_mode :
							mode = "leftMode";
							currentHandModeTextView
									.setText(R.string.settingLeftMode);
							break;
						case R.id.right_mode :
							mode = "rightMode";
							currentHandModeTextView
									.setText(R.string.settingRightMode);
							break;
						case R.id.default_mode :
							currentHandModeTextView
									.setText(R.string.settingDefaultMode);
							break;
						default :
							break;
					}
					Editor shareEditor = sharedPreferences.edit();
					shareEditor.putString("openMode", mode);
					shareEditor.commit();
				}

			};

			new AlertDialog.Builder(SetActivity.this)
					.setTitle(R.string.settingTextTips).setView(contentView)
					.setPositiveButton(R.string.ok, okListener)
					.setNegativeButton(R.string.cancel, null).show();
		}
	};

	/**
	 * ÉèÖÃ´óÐ¡°´Å¥¼àÌýÆ÷
	 */
	private OnClickListener setSizeListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			LayoutInflater inflater = getLayoutInflater();
			final View contentView = inflater.inflate(R.layout.set_text_size,
					null);
			final Editor editor = sharedPreferences.edit();
			final SeekBar resizeSeekBar = (SeekBar) contentView
					.findViewById(R.id.resizeSeekbar);
			resizeSeekBar.setMax(30);

			final TextView demoTextView = (TextView) contentView
					.findViewById(R.id.demoText);
			int size = 0;
			switch (v.getId()) {
				case R.id.setSmall :
					size = sharedPreferences.getInt("smallSize",
							Constants.DEFAULT_SMALL_SIZE);
					break;
				case R.id.setMedium :
					size = sharedPreferences.getInt("mediumSize",
							Constants.DEFAULT_MEDIUM_SIZE);
					break;
				case R.id.setLarge :
					size = sharedPreferences.getInt("largeSize",
							Constants.DEFAULT_LARGE_SIZE);
					break;
				default :
					break;
			}
			demoTextView.setText(getString(R.string.app_name) + "(" + size
					+ ")");
			demoTextView.setTextSize((float) size);
			resizeSeekBar.setProgress(size - 10);
			resizeSeekBar
					.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onProgressChanged(SeekBar seekBar,
								int progress, boolean fromUser) {
							demoTextView.setTextSize((float) progress + 10);
							demoTextView.setText(getResources().getString(
									R.string.app_name)
									+ "(" + (progress + 10) + ")");
						}
					});
			DialogInterface.OnClickListener onDialogOKClickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					switch (v.getId()) {
						case R.id.setSmall :
							editor.putInt("smallSize",
									resizeSeekBar.getProgress() + 10);
							break;
						case R.id.setMedium :
							editor.putInt("mediumSize",
									resizeSeekBar.getProgress() + 10);
							break;
						case R.id.setLarge :
							editor.putInt("largeSize",
									resizeSeekBar.getProgress() + 10);
							break;
						default :
							break;
					}
					editor.commit();
				}
			};
			new AlertDialog.Builder(SetActivity.this)
					.setTitle(R.string.settingTextTips).setView(contentView)
					.setPositiveButton(R.string.ok, onDialogOKClickListener)
					.setNegativeButton(R.string.cancel, null).show();
		}
	};
}
