package com.zhan_dui.dictionary.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.adapters.OnlineListCursorAdapter;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.filemanager.FileSelectorActivity;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.JsonGetter;

/**
 * 
 * @author xuanqinanhai
 * 
 */
public class OnlineDictionaryActivity extends Activity
		implements
			OnClickListener

{

	private OnlineInfoHandler onlineInfoHandler;
	private ProgressDialog progressDialog;
	private ListView dictionaryList;
	private Context context;
	private Button importDictionary;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		onlineInfoHandler = new OnlineInfoHandler();
		setContentView(R.layout.online_dictionary);

		dictionaryList = (ListView) findViewById(R.id.list_online_dictionary);
		dictionaryList.setSelector(R.drawable.item_dictionary_selector);
		importDictionary = (Button) findViewById(R.id.import_dictionary);
		new GetOnlineListThread().start();
		importDictionary.setOnClickListener(this);
	}
	SQLiteDatabase sqLiteDatabase = null;
	/**
	 * 在线词典获取时候的Handler
	 * 
	 * @author xuanqinanhai
	 * 
	 */
	@SuppressLint("HandlerLeak")
	class OnlineInfoHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case Constants.DOWNLOAD_ERROR :
					Toast.makeText(context,
							context.getString(R.string.progress_error),
							Toast.LENGTH_LONG).show();
					if (progressDialog != null) {
						progressDialog.dismiss();
					}
					break;
				case Constants.DOWNLOAD_FINISH :
					if (progressDialog != null) {
						progressDialog.dismiss();
					}
					sqLiteDatabase = new DictionaryDB(
							OnlineDictionaryActivity.this,
							DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION)
							.getReadableDatabase();
					Cursor cursor = sqLiteDatabase.rawQuery(
							"select * from dictionary_list", null);
					OnlineListCursorAdapter onlineListCursorAdapter = new OnlineListCursorAdapter(
							OnlineDictionaryActivity.this, cursor);
					dictionaryList.setAdapter(onlineListCursorAdapter);
					break;
				case Constants.DOWNLOAD_SUCCESS :
					if (progressDialog != null) {
						Toast.makeText(OnlineDictionaryActivity.this,
								getString(R.string.progress_success),
								Toast.LENGTH_LONG).show();
					}
					break;
				case Constants.DOWNLOADING :
					progressDialog = ProgressDialog.show(
							OnlineDictionaryActivity.this,
							getString(R.string.progress_downloading),
							getString(R.string.progress_wait));
					progressDialog.setCancelable(true);
					break;
				default :
					break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (sqLiteDatabase != null) {
			sqLiteDatabase.close();
		}
	}

	/**
	 * 获取在线词典列表线程
	 * 
	 * @author xuanqinanhai
	 * 
	 */
	class GetOnlineListThread extends Thread {

		/**
		 * 将获取的字典字符串数据转换成json，并且存储
		 * 
		 * @param json
		 * @throws JSONException
		 */
		public void ParseJson(String json) throws JSONException {
			JSONArray jsonArray = new JSONArray(json);
			String name, size, url, save_name;
			JSONObject jsonObject;
			DictionaryDB dictionaryDB = new DictionaryDB(
					OnlineDictionaryActivity.this, DictionaryDB.DB_NAME, null,
					DictionaryDB.DB_VERSION);
			SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();

			for (int i = 0; i < jsonArray.length(); i++) {

				Cursor cursor = sqLiteDatabase.rawQuery(
						"select count(*) from dictionary_list", null);
				cursor.moveToFirst();
				int dictionaryCount = cursor.getInt(cursor
						.getColumnIndex("count(*)"));

				jsonObject = jsonArray.getJSONObject(i);
				name = jsonObject.getString("dictionary_name");
				size = jsonObject.getString("dictionary_size");
				url = jsonObject.getString("dictionary_url");
				save_name = jsonObject.getString("dictionary_save_name");
				String checkIfExsit = "select * from dictionary_list where `dictionary_name`='"
						+ name + "'";

				cursor = sqLiteDatabase.rawQuery(checkIfExsit, null);
				ContentValues contentValues = new ContentValues();
				contentValues.put("dictionary_name", name);
				contentValues.put("dictionary_size", size);
				contentValues.put("dictionary_url", url);
				contentValues.put("dictionary_save_name", save_name);
				if (cursor.getCount() == 0) {
					contentValues.put("dictionary_order", dictionaryCount);
					sqLiteDatabase.insert("dictionary_list", null,
							contentValues);
				} else {
					String[] args = {name};
					sqLiteDatabase.update("dictionary_list", contentValues,
							"dictionary_name=?", args);
				}
			}
			sqLiteDatabase.close();

		}
		@Override
		public void run() {
			super.run();
			Message msg = new Message();
			msg.what = Constants.DOWNLOADING;
			msg.setTarget(onlineInfoHandler);
			msg.sendToTarget();
			SQLiteDatabase sqLiteDatabase = null;
			try {
				String jsonString = JsonGetter
						.get(Constants.ONLINE_DICTIONARY_LIST_URL);

				Message msg_success = new Message();
				ParseJson(jsonString);
				msg_success.setTarget(onlineInfoHandler);
				msg_success.what = Constants.DOWNLOAD_SUCCESS;
				msg_success.sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
				Message msg_error = new Message();
				msg_error.setTarget(onlineInfoHandler);
				msg_error.what = Constants.DOWNLOAD_ERROR;
				msg_error.sendToTarget();
			} finally {
				Message msg_finish = new Message();
				msg_finish.setTarget(onlineInfoHandler);
				msg_finish.what = Constants.DOWNLOAD_FINISH;
				msg_finish.sendToTarget();
				if (sqLiteDatabase != null) {
					sqLiteDatabase.close();
				}
			}

		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.import_dictionary :
				Intent intent = new Intent(OnlineDictionaryActivity.this,
						FileSelectorActivity.class);
				startActivity(intent);
				break;

			default :
				break;
		}
	}

}
