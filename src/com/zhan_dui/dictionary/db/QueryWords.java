package com.zhan_dui.dictionary.db;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
/**
 * 
 * @Description:主要用来实现查询功能
 * @date 2012-11-18 上午10:26:44
 */
public class QueryWords implements Runnable {
	private Handler handler;
	private Context context;
	private String word;

	public static int QUERY_SUCCESS = 1;
	public static int QUERY_ERROR = 2;
	public static int QUERY_NO_DICT = 3;

	public QueryWords(Context context, Handler handler, String word) {
		this.context = context;
		this.handler = handler;
		this.word = word;
	}
	/**
	 * 
	 * @Description:用作返回数据
	 * @date 2012-11-18 上午10:27:05
	 */
	public class LayoutInformation {
		public LinearLayout contentLayout;
		public String dictionaryName;
		public String word;
		public LayoutInformation(LinearLayout contentLayout,
				String dictionaryName, String word) {
			super();
			this.contentLayout = contentLayout;
			this.dictionaryName = dictionaryName;
			this.word = word;
		}

	}

	public void run() {
		DictionaryDB dictionaryDB = new DictionaryDB(context,
				DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getReadableDatabase();
		Cursor cursor = sqLiteDatabase
				.rawQuery(
						"select * from dictionary_list where dictionary_downloaded='1' order by dictionary_order asc",
						null);
		Message msg = null;

		// 如果没有词典可以查询，会返回QUERY_NO_DICT，根据此消息引导用户去下载词典
		if (cursor.getCount() == 0) {
			msg = Message.obtain(handler, QUERY_NO_DICT);
			msg.sendToTarget();
			return;
		}

		while (cursor.moveToNext()) {
			String saveName = cursor.getString(cursor
					.getColumnIndex("dictionary_save_name"));
			String dictionaryName = cursor.getString(cursor
					.getColumnIndex("dictionary_name"));
			String file_name = saveName.replace("zip", "dic");
			String config_file_name = "config-" + file_name;
			LinearLayout linearLayout = null;
			try {
				try {
					linearLayout = (LinearLayout) dictionaryDB.queryWord(
							context, word, file_name, config_file_name);
					LayoutInformation layoutInformation = new LayoutInformation(
							linearLayout, dictionaryName, word);
					msg = Message.obtain(handler, QUERY_SUCCESS,
							layoutInformation);
					msg.sendToTarget();

				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		sqLiteDatabase.close();
	}
};