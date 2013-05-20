package com.zhan_dui.dictionary.activity;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.adapters.NewWordsCursorAdapter;
import com.zhan_dui.dictionary.adapters.ViewPagerAdapter;
import com.zhan_dui.dictionary.custom.VerticalTextView;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.db.QueryWords;
import com.zhan_dui.dictionary.db.QueryWords.LayoutInformation;
import com.zhan_dui.dictionary.handlers.UnzipHandler;
import com.zhan_dui.dictionary.listeners.RightDrawableOnTouchListener;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.DisplayUtils;
import com.zhan_dui.dictionary.utils.UnzipFileInThread;

/**
 * 
 * @ClassName:MainActivity.java
 * @Description:
 * @author xuanqinanhai
 * @date 2012-11-9 上午9:13:52
 */
@SuppressLint("HandlerLeak")
public class MainActivity extends Activity {

	private ViewPagerAdapter myAdapter = new ViewPagerAdapter();
	private Button btn_query_word, btn_words_list;
	private View line_btn_query_word, line_btn_words_list;
	private final int line_ids[] = {R.id.line_btn_query_word,
			R.id.line_btn_words_list};
	private Context context;
	private ImageView add_word;// 添加生词按钮
	private GridView newWordsGridView;// 生词本GridView
	private ViewPager viewPager;
	private WordClickListener wordClickListener;// 生词本按钮事件
	private SharedPreferences sharedPreferences;
	private EditText queryEditText;// 单词输入框
	private boolean addNewWordFlag = false;// 是否添加了生词的标识符，这样可以阻止反复读取数据库

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		sharedPreferences = context.getSharedPreferences(Constants.PREF_FIRST,
				Context.MODE_PRIVATE);
		setContentView(R.layout.activity_main);

		viewPager = (ViewPager) findViewById(R.id.viewpager);

		viewPager.setOnPageChangeListener(new ViewPagerScroolListener());

		line_btn_query_word = (View) findViewById(R.id.line_btn_query_word);
		line_btn_words_list = (View) findViewById(R.id.line_btn_words_list);

		btn_words_list = (Button) findViewById(R.id.btn_words_list);
		btn_query_word = (Button) findViewById(R.id.btn_query_word);
		// 单词按钮切换监听器
		btn_query_word.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(0);
				line_btn_query_word
						.setBackgroundResource(R.color.navigate_line_green);
				line_btn_words_list.setBackgroundResource(R.color.gray);
			}
		});
		// 生词本按钮切换监听器
		btn_words_list.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(1);
				line_btn_words_list
						.setBackgroundResource(R.color.navigate_line_green);
				line_btn_query_word.setBackgroundResource(R.color.gray);
			}
		});
		// 检查必须的词典是否存在
		CheckWordExist();
	}


	/**
	 * 首先检查是否有SD卡，如果没有SD卡，则提示退出
	 * 检查基础词库是否存在，如果不存在，则将assets中的zip文件解压到sd卡的Constant.SAVE_DIRECTORY目录中
	 */
	private void CheckWordExist() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED) == false) {
			// 不存在SD卡
			AlertDialog.Builder alertbuBuilder = new AlertDialog.Builder(this);
			alertbuBuilder.setMessage(R.string.no_sd_alert);
			alertbuBuilder.setTitle(R.string.alert);
			alertbuBuilder.setPositiveButton(R.string.close,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alertbuBuilder.setCancelable(false);
			alertbuBuilder.show();
		} else {
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/" + Constants.SAVE_DIRECTORY + "/"
					+ Constants.BASE_DICTIONARY);
			if (file.exists() == false) {
				try {
					InputStream inputStream = context.getAssets().open(
							Constants.BASE_DICTIONARY_ASSET);
					new UnzipFileInThread(new UnzipHandler(MainActivity.this),
							inputStream,
							Environment.getExternalStorageDirectory() + "/"
									+ Constants.SAVE_DIRECTORY + "/", true)
							.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 菜单按钮
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_close :
				finish();
				break;
			case R.id.menu_online_dictionary :
				startActivity(new Intent(this, OnlineDictionaryActivity.class));
				break;
			case R.id.menu_settings :
				startActivity(new Intent(this, SetActivity.class));
			default :
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 
	 * @Description:搜索按钮按下时候的监听器
	 * @date 2012-11-9 上午9:20:28
	 */
	class QueryWordButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			String word = ((EditText) MainActivity.this
					.findViewById(R.id.edt_search_word)).getText().toString();
			if (word.length() == 0) {
				return;
			}
			// 关闭软键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(queryEditText.getWindowToken(), 0);

			FrameLayout parentLayout = (FrameLayout) MainActivity.this
					.findViewById(R.id.dictionary_meaning_content);
			parentLayout.removeAllViewsInLayout();
			LinearLayout namesLayout = (LinearLayout) MainActivity.this
					.findViewById(R.id.button_container);
			namesLayout.removeAllViews();

			new Thread(new QueryWords(context, new QueryHandler(parentLayout,
					namesLayout), word)).start();
		}

	}
	public class QueryHandler extends Handler {

		private FrameLayout parentLayout;
		private LinearLayout namesLayout;
		private HashMap<String, LinearLayout> layouts = new HashMap<String, LinearLayout>();
		private String currentDictionary = null;

		public QueryHandler(FrameLayout parentLayout, LinearLayout namesLayout) {
			super();
			this.parentLayout = parentLayout;
			this.namesLayout = namesLayout;
		}

		@SuppressLint("HandlerLeak")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == QueryWords.QUERY_SUCCESS) {
				final LayoutInformation layoutInformation = (LayoutInformation) msg.obj;
				if (currentDictionary == null) {
					parentLayout.addView(layoutInformation.contentLayout);
					currentDictionary = layoutInformation.dictionaryName;
					String word = layoutInformation.word;
					add_word.setContentDescription(word);
				}

				TextView nameView;
				LayoutParams layoutParams;
				String openMode = sharedPreferences.getString("openMode",
						"defaultMode");

				if (openMode.equalsIgnoreCase("defaultMode")) {
					nameView = new TextView(context);
					layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.FILL_PARENT);
				} else {
					nameView = new VerticalTextView(context);
					if (openMode.equalsIgnoreCase("rightMode")) {
						((VerticalTextView) nameView)
								.setToward(VerticalTextView.TOWARD_LEFT);
					} else {
						((VerticalTextView) nameView)
								.setToward(VerticalTextView.TOWARD_RIGHT);
					}
					layoutParams = new LayoutParams(DisplayUtils.dip2px(
							context, 30), LayoutParams.WRAP_CONTENT);
				}
				layouts.put(layoutInformation.dictionaryName,
						layoutInformation.contentLayout);

				nameView.setLayoutParams(layoutParams);
				nameView.setPadding(5, 0, 5, 0);
				nameView.setTextColor(getResources().getColor(
						R.color.navigate_line_green));
				nameView.setGravity(Gravity.CENTER);
				nameView.setText(layoutInformation.dictionaryName);
				nameView.setGravity(Gravity.CENTER);
				nameView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						parentLayout.removeAllViews();
						parentLayout.addView(layouts
								.get(layoutInformation.dictionaryName));
					}
				});
				namesLayout.addView(nameView);

			} else if (msg.what == QueryWords.QUERY_NO_DICT) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(R.string.no_dict);
				builder.setTitle(R.string.alert);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.to_download,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent();
								intent.setClass(MainActivity.this,
										OnlineDictionaryActivity.class);
								MainActivity.this.startActivity(intent);
							}
						});
				builder.setNegativeButton(R.string.close,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				builder.show();
			}
		}
	}
	/**
	 * 
	 * @ClassName:MainActivity.java
	 * @Description:ViewPager滑动监听，主要是改变底线的属性
	 * @date 2012-11-9 上午9:19:55
	 */
	class ViewPagerScroolListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int position) {

		}

		@Override
		public void onPageScrolled(int before, float arg1, int after) {

		}

		@Override
		public void onPageSelected(int position) {
			for (int i = 0; i < line_ids.length; i++) {
				findViewById(line_ids[i]).setBackgroundResource(R.color.gray);
			}
			findViewById(line_ids[position]).setBackgroundResource(
					R.color.navigate_line_green);
			if (addNewWordFlag) {
				DictionaryDB dictionaryDB = new DictionaryDB(context,
						DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
				SQLiteDatabase sqLiteDatabase = dictionaryDB
						.getWritableDatabase();
				Cursor cursor = sqLiteDatabase.rawQuery("select * from word",
						null);
				newWordsGridView.setAdapter(new NewWordsCursorAdapter(context,
						cursor, true, wordClickListener));
				sqLiteDatabase.close();
				addNewWordFlag = false;
			}
		}
	}

	class AddWordListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			String contentDescription = (String) v.getContentDescription();
			if (contentDescription != null) {
				DictionaryDB dictionaryDB = new DictionaryDB(context,
						DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
				int result = dictionaryDB.addWord(contentDescription);
				switch (result) {
					case DictionaryDB.WORD_EXSIST :
						Toast.makeText(context, "单词已存在", Toast.LENGTH_SHORT)
								.show();
						break;
					case DictionaryDB.WORD_ADDED :
						Toast.makeText(context, "单词添加成功", Toast.LENGTH_SHORT)
								.show();
						addNewWordFlag = true;
						break;
					default :
						break;
				}
			}
		}

	}
	/**
	 * 点生词时候的监听器
	 * 
	 * @Description:
	 * @date 2012-11-13 下午2:37:48
	 */
	private class WordClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			String word = (String) v.getContentDescription();
			viewPager.setCurrentItem(0);
			line_btn_query_word
					.setBackgroundResource(R.color.navigate_line_green);
			line_btn_words_list.setBackgroundResource(R.color.gray);
			FrameLayout parentLayout = (FrameLayout) MainActivity.this
					.findViewById(R.id.dictionary_meaning_content);
			parentLayout.removeAllViewsInLayout();
			LinearLayout namesLayout = (LinearLayout) MainActivity.this
					.findViewById(R.id.button_container);
			namesLayout.removeAllViews();
			((EditText) MainActivity.this.findViewById(R.id.edt_search_word))
					.setText(word);
			new Thread(new QueryWords(context, new QueryHandler(parentLayout,
					namesLayout), word)).start();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		myAdapter.clear();
		myAdapter.addPageView(initQueryXML());
		myAdapter.addPageView(initWordsXML());
		viewPager.setAdapter(myAdapter);
	}

	private View currentQueryWordView;
	private View currentNewWordsView;

	protected View initWordsXML() {
		if (currentNewWordsView != null) {
			return currentNewWordsView;
		}
		View new_words_list = LayoutInflater.from(this).inflate(
				R.layout.new_words_list, null);
		newWordsGridView = (GridView) new_words_list
				.findViewById(R.id.new_words_list);
		wordClickListener = new WordClickListener();
		DictionaryDB dictionaryDB = new DictionaryDB(context,
				DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
		SQLiteDatabase sqLiteDatabase = dictionaryDB.getWritableDatabase();
		Cursor cursor = sqLiteDatabase.rawQuery("select * from word", null);
		newWordsGridView.setAdapter(new NewWordsCursorAdapter(context, cursor,
				true, wordClickListener));
		sqLiteDatabase.close();
		currentNewWordsView = new_words_list;
		return currentNewWordsView;
	}

	protected View initQueryXML() {
		View query_word_view = null;
		String openMode = sharedPreferences
				.getString("openMode", "defaultMode");
		if (currentQueryWordView == null
				|| currentQueryWordView.getContentDescription()
						.equals(openMode) == false) {
			if (openMode.equalsIgnoreCase("rightMode")) {
				query_word_view = LayoutInflater.from(this).inflate(
						R.layout.query_word_right, null);
			} else if (openMode.equalsIgnoreCase("leftMode")) {
				query_word_view = LayoutInflater.from(this).inflate(
						R.layout.query_word_left, null);
			} else {
				query_word_view = LayoutInflater.from(this).inflate(
						R.layout.query_word, null);
			}

			query_word_view.findViewById(R.id.search).setOnClickListener(
					new QueryWordButtonListener());
			queryEditText = (EditText) query_word_view
					.findViewById(R.id.edt_search_word);
			add_word = (ImageView) query_word_view.findViewById(R.id.add_word);
			// 设置添加生词按钮的监听器
			add_word.setOnClickListener(new AddWordListener());

			// 输入框的删除按钮功能
			queryEditText.setOnTouchListener(new RightDrawableOnTouchListener(
					queryEditText) {

				@Override
				public boolean onDrawableTouch(MotionEvent event) {
					queryEditText.setText("");
					return true;
				}
			});
			currentQueryWordView = query_word_view;
			currentQueryWordView.setContentDescription(openMode);
		} else {
			query_word_view = currentQueryWordView;
		}
		return query_word_view;
	}

	/**
	 * 文件移动时候的Handler
	 */
	@SuppressLint("HandlerLeak")
	Handler MoveHandler = new Handler() {
		private ProgressDialog progressDialog;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == Constants.MOVE_START) {
				progressDialog = new ProgressDialog(MainActivity.this);
				progressDialog.setTitle(R.string.move_dialog_title);
				progressDialog
						.setMessage(getString(R.string.move_dialog_content));
				progressDialog.setCancelable(true);
				progressDialog
						.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setMax(msg.arg1);
				progressDialog.show();
			} else if (msg.what == Constants.MOVING) {
				progressDialog.setProgress(msg.arg1);
			} else if (msg.what == Constants.MOVE_END) {
				progressDialog.dismiss();
			} else if (msg.what == Constants.MOVE_ERROR) {
				Toast.makeText(getApplicationContext(), "error",
						Toast.LENGTH_SHORT).show();
			}
			msg = null;
		}
	};
}
