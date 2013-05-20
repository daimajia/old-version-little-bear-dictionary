package com.zhan_dui.dictionary.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.db.DictionaryParseInfomation.EchoViews;
import com.zhan_dui.dictionary.db.DictionaryParseInfomation.TextArg;
import com.zhan_dui.dictionary.handlers.DictionaryXMLHandler;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.DisplayUtils;

@SuppressLint("DefaultLocale")
public class DictionaryDB extends SQLiteOpenHelper {

	private final static String DB_PATH = Environment
			.getExternalStorageDirectory()
			+ "/"
			+ Constants.SAVE_DIRECTORY
			+ "/";
	private final Context context;
	public final static String DB_NAME = "dictionary";
	public final static int DB_VERSION = 3;
	public final static String DB_BASE_DIC = "dictionary_word.sqlite";
	public final static String DB_DICTIONARY_LIST_NAME = "dictionary_list";
	private SharedPreferences sharedPreferences;

	public DictionaryDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.context = context;
		sharedPreferences = context.getSharedPreferences(Constants.PREF_FIRST,
				Context.MODE_PRIVATE);
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		String createSql = "create table if not exists dictionary_list (_id INTEGER PRIMARY KEY AUTOINCREMENT,dictionary_name text,dictionary_size text,dictionary_url text,dictionary_save_name text,dictionary_downloaded INTEGER default 0,dictionary_show INTEGER default 0,dictionary_order INTEGER default 0);";
		String createSql_words = "create table if not exists  word(_id INTEGER PRIMARY KEY AUTOINCREMENT,word text);";
		sqLiteDatabase.execSQL(createSql);
		sqLiteDatabase.execSQL(createSql_words);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public static final int WORD_EXSIST = 1;
	public static final int WORD_ADDED = 2;

	/**
	 * addWord 添加生词进生词本
	 * 
	 * @param word
	 * @return
	 */
	public int addWord(String word) {
		DictionaryDB dictionaryDB = new DictionaryDB(context,
				DictionaryDB.DB_NAME, null, DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
		String[] whereArgs = {word};
		Cursor cursor = sqLiteDatabase.query("word", null, "word=?", whereArgs,
				null, null, null);
		int result = 0;
		if (cursor.getCount() == 0) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("word", word);
			sqLiteDatabase.insert("word", null, contentValues);
			result = WORD_ADDED;
		} else {
			result = WORD_EXSIST;
		}
		sqLiteDatabase.close();
		return result;
	}

	/**
	 * deleteWord 丛生此表中删除某个单词
	 * 
	 * @param word
	 */
	public void deleteWord(String word) {
		DictionaryDB dictionaryDB = new DictionaryDB(context,
				DictionaryDB.DB_NAME, null, DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
		String sql = "delete from word where word='" + word + "'";
		sqLiteDatabase.execSQL(sql);
		sqLiteDatabase.close();
	}

	/**
	 * 在基础词库中查询一个单词的ID
	 * 
	 * @param sqLiteDatabase
	 *            数据库链接
	 * @param word
	 *            单词
	 * @return 返回一个int代表单词的id
	 */
	private int queryWordId(SQLiteDatabase sqLiteDatabase, String word) {
		String[] tableStrings = {"id"};
		Cursor cursor = sqLiteDatabase.query("word", tableStrings, "word='"
				+ word + "'", null, null, null, null);
		if (cursor.moveToNext()) {
			Log.i("word_id", cursor.getInt(0) + "");
			int id = cursor.getInt(0);

			return id;
		}
		return -1;
	}
	/**
	 * 缓存XML文件信息，可以缩短查询时间
	 */
	private HashMap<String, DictionaryParseInfomation> cacheXMLInformation = new HashMap<String, DictionaryParseInfomation>();
	/**
	 * 查询单词，根据配置的XML文件查询
	 * 
	 * @param context
	 *            上下文
	 * @param word
	 *            单词
	 * @param xmlfile
	 *            配置文件
	 * @return 返回一个排版好的View，直接用来添加到query_word.xml中
	 * @throws ParserConfigurationException
	 *             XML文件转换异常
	 * @throws SAXException
	 *             SAX转换异常
	 * @throws IOException
	 */
	public View queryWord(Context context, String word, String sqliteFileName,
			String xmlFileName) throws ParserConfigurationException,
			SAXException, IOException {

		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		DictionaryXMLHandler dictionaryXMLHandler = new DictionaryXMLHandler();
		DictionaryParseInfomation dictionaryParseInfomation;

		if (cacheXMLInformation.containsKey(sqliteFileName)) {
			dictionaryParseInfomation = cacheXMLInformation.get(sqliteFileName);
		} else {
			String xmlFilePath = Environment.getExternalStorageDirectory()
					+ "/" + Constants.SAVE_DIRECTORY + "/" + xmlFileName;
			saxParser.parse(new File(xmlFilePath), dictionaryXMLHandler);
			dictionaryParseInfomation = dictionaryXMLHandler.getResults();
			cacheXMLInformation.put(sqliteFileName, dictionaryParseInfomation);
		}

		SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(
				Environment.getExternalStorageDirectory() + "/"
						+ Constants.SAVE_DIRECTORY + "/" + sqliteFileName,
				null, SQLiteDatabase.OPEN_READWRITE);

		String table = dictionaryParseInfomation.table;
		String[] columns = (String[]) (dictionaryParseInfomation.queryWords
				.toArray(new String[0]));

		SQLiteDatabase iddatabase = SQLiteDatabase.openDatabase(DB_PATH
				+ DB_BASE_DIC, null, SQLiteDatabase.OPEN_READWRITE);
		int word_id = queryWordId(iddatabase, word);
		Log.i("word_id", word_id + "");
		iddatabase.close();

		String[] selectionArgs = {word_id + ""};
		Cursor cursor = sqLiteDatabase.query(table, columns, "word_id=?",
				selectionArgs, null, null, null);

		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		ViewGroup.LayoutParams layoutParams = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		linearLayout.setLayoutParams(layoutParams);
		TextView titleView = new TextView(context);
		titleView.setText(dictionaryParseInfomation.title);
		titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		titleView.setTextColor(context.getResources().getColor(
				R.color.lightblack));

		linearLayout.addView(titleView);
		int counter = 1;
		while (cursor.moveToNext()) {// 对检索到的数据做相同的处理
			for (EchoViews echoView : dictionaryParseInfomation.echoViews) {// 遍历配置中需要输出的每个View
				if (echoView.viewType.equalsIgnoreCase("textview")) {
					TextView textView = new TextView(context);
					textView.setLayoutParams(layoutParams);

					textView.setPadding(echoView.view_padding_left,
							echoView.view_padding_top,
							echoView.view_padding_right,
							echoView.view_padding_bottom);

					ArrayList<SpannableString> contents = new ArrayList<SpannableString>();
					int left = 0, right = 0, top = 0, bottom = 0;
					for (TextArg arg : echoView.sprintfArgs) {
						left = DisplayUtils.dip2px(context,
								arg.text_padding_left);
						right = DisplayUtils.dip2px(context,
								arg.text_padding_right);
						top = DisplayUtils
								.dip2px(context, arg.text_padding_top);
						bottom = DisplayUtils.dip2px(context,
								arg.text_padding_bottom);

						String content = cursor.getString(cursor
								.getColumnIndex(arg.argContent));
						SpannableString spannableContentString = new SpannableString(
								content);
						if (arg.action != null) {
							if (arg.action.equals("split")) {
								// |||
								String[] examples = content.split("\\|\\|\\|");
								content = "\n\n";
								for (String example : examples) {
									content += example + "\n";
								}
								spannableContentString = new SpannableString(
										content);
							}
						}
						CharacterStyle characterStyle;

						String textColor = arg.textColor;

						characterStyle = new ForegroundColorSpan(
								Color.parseColor(textColor));

						spannableContentString.setSpan(characterStyle, 0,
								spannableContentString.length(),
								Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

						int size = 0;

						if (arg.textSize.equalsIgnoreCase("normal")) {
							size = sharedPreferences.getInt("mediumSize",
									Constants.DEFAULT_MEDIUM_SIZE);
						} else if (arg.textSize.equalsIgnoreCase("small")) {
							size = sharedPreferences.getInt("smallSize",
									Constants.DEFAULT_SMALL_SIZE);
						} else if (arg.textSize.equalsIgnoreCase("large")) {
							size = sharedPreferences.getInt("largeSize",
									Constants.DEFAULT_LARGE_SIZE);
						}

						size = DisplayUtils.dip2px(context, size);

						characterStyle = new AbsoluteSizeSpan(size);
						spannableContentString.setSpan(characterStyle, 0,
								spannableContentString.length(),
								Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

						if (arg.textStyle.equalsIgnoreCase("bold")) {
							characterStyle = new StyleSpan(Typeface.BOLD);
						} else if (arg.textStyle.equalsIgnoreCase("italic")) {
							characterStyle = new StyleSpan(Typeface.ITALIC);
						} else if (arg.textStyle.equalsIgnoreCase("underline")) {
							characterStyle = new UnderlineSpan();
						}

						spannableContentString.setSpan(characterStyle, 0,
								spannableContentString.length(),
								Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						contents.add(spannableContentString);
					}
					CharSequence resultContent = TextUtils.concat(contents
							.toArray(new SpannableString[0]));
					SpannableString leaderIndex = new SpannableString(counter++
							+ ".");
					leaderIndex.setSpan(
							new ForegroundColorSpan(context.getResources()
									.getColor(R.color.navigate_line_green)), 0,
							leaderIndex.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					leaderIndex.setSpan(new StyleSpan(Typeface.BOLD), 0,
							leaderIndex.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					resultContent = TextUtils
							.concat(leaderIndex, resultContent);

					textView.setText(resultContent);
					textView.setPadding(left, top, right, bottom);

					linearLayout.addView(textView);
				}
			}
		}
		cursor.close();
		sqLiteDatabase.close();
		return linearLayout;
	}
	public View queryWord(String word) {
		if (word.length() == 0) {
			return null;
		}
		SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(DB_PATH
				+ DB_BASE_DIC, null, SQLiteDatabase.OPEN_READONLY);
		int word_id = queryWordId(sqLiteDatabase, word);
		String[] tableString = {"simple_meaning"};
		String[] selectionArgs = {word_id + ""};
		Cursor cursor = sqLiteDatabase.query("simpledic", tableString,
				"word_id=?", selectionArgs, null, null, null);
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		ViewGroup.LayoutParams layoutParams = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		int count = 1;

		if (cursor.getCount() != 0) {
			TextView titleView = new TextView(context);
			titleView.setText("简明释义:");
			titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			titleView.setTextColor(context.getResources().getColor(
					R.color.lightblack));
			linearLayout.addView(titleView);
		}

		while (cursor.moveToNext()) {
			TextView textView = new TextView(context);
			textView.setText(count++ + "." + cursor.getString(0));
			textView.setPadding(10, 0, 0, 0);
			textView.setLayoutParams(layoutParams);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			textView.setTextColor(context.getResources()
					.getColor(R.color.black));
			linearLayout.addView(textView);
		}
		cursor.close();
		sqLiteDatabase.close();
		return linearLayout;
	}

}
