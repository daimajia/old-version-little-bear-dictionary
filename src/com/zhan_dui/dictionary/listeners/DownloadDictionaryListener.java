package com.zhan_dui.dictionary.listeners;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CursorAdapter;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.adapters.OnlineListCursorAdapter;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.utils.Constants;
import com.zhan_dui.dictionary.utils.DownloadUtils;
import com.zhan_dui.dictionary.utils.DownloadUtils.DownloadUtilsInterface;
import com.zhan_dui.dictionary.utils.UnzipFile;
/**
 * 下载按钮监听器，点下后调用下载线程启动，同时激活下载Notification
 * 
 * @author xuanqinanhai
 * 
 */
public class DownloadDictionaryListener implements OnClickListener {

	private int id;
	private String dictionaryName;
	private String dictionaryUrl;
	private String dictionarySize;
	private String dictionarySaveName;
	private Context context;
	private CursorAdapter currentAdapter;

	public DownloadDictionaryListener(Context context,
			CursorAdapter currentAdapter, int id, String dictionaryName,
			String dictionarySaveName, String dictionaryUrl,
			String dictionarySize) {
		super();
		this.context = context;
		this.currentAdapter = currentAdapter;
		this.id = id;
		this.dictionaryName = dictionaryName;
		this.dictionarySaveName = dictionarySaveName;
		this.dictionaryUrl = dictionaryUrl;
		this.dictionarySize = dictionarySize;
	}

	@Override
	public void onClick(View v) {
		String savePath = Environment.getExternalStorageDirectory() + "/"
				+ Constants.SAVE_DIRECTORY + "/" + dictionarySaveName;
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification();
		notification.icon = android.R.drawable.stat_sys_download;
		notification.tickerText = context.getText(R.string.start_download)
				+ dictionaryName + context.getText(R.string.download_size)
				+ dictionarySize;
		RemoteViews contentView = new RemoteViews(context.getPackageName(),
				R.layout.notification_progress);
		notification.contentView = contentView;
		// 使用自定义下拉视图时，不需要再调用setLatestEventInfo()方法
		// 但是必须定义 contentIntent
		Intent intent = new Intent();
		PendingIntent pd = PendingIntent.getActivity(context, 0, intent, 0);
		notification.contentIntent = pd;
		notification.contentView.setTextViewText(
				R.id.txt_download_dictionary_name, dictionaryName);
		notificationManager.notify(id, notification);
		DownloadUtils.download(dictionaryUrl, savePath, new DownloadBehavior(
				notificationManager, notification, id), notificationManager,
				notification, R.id.download_progress, id);
	}

	/**
	 * @Description:下载过程中的行为监听，实现了DownloadUtilsInterface
	 * @date 2012-11-28 下午2:51:50
	 */
	@SuppressLint("HandlerLeak")
	class DownloadBehavior implements DownloadUtilsInterface {

		private Boolean status = true;
		private NotificationManager notificationManager;
		private Notification notification;
		private int notificationId;

		public DownloadBehavior(NotificationManager notificationManager,
				Notification notification, int notificationId) {
			this.notificationManager = notificationManager;
			this.notification = notification;
			this.notificationId = notificationId;
		}
		/**
		 * 下载之前执行的行为，主要是修改下载状态列表
		 */
		@Override
		public void beforeDownload(String url) {
			OnlineListCursorAdapter.downloadingNotificationUrls.add(url);
			currentAdapter.notifyDataSetChanged();
		}
		/**
		 * 下载结束时候的行为。主要是将下载好的字典添加到数据库中。
		 */
		@Override
		public void afterDownload(Boolean result, String url, String savePath) {
			OnlineListCursorAdapter.downloadingNotificationUrls.remove(url);
			currentAdapter.notifyDataSetChanged();
			if (result && status) {
				DictionaryDB dictionaryDB = new DictionaryDB(context,
						DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
				SQLiteDatabase sqLiteDatabase = dictionaryDB
						.getWritableDatabase();
				ContentValues contentValues = new ContentValues();
				contentValues.put("dictionary_downloaded", "1");

				String args[] = {url};
				sqLiteDatabase.update(DictionaryDB.DB_DICTIONARY_LIST_NAME,
						contentValues, "dictionary_url=?", args);
				sqLiteDatabase.close();
			}
		}

		/**
		 * 出错处理，如果下载出错则执行，返回一个是出错消息，可以直接显示，一个是出错的下载链接
		 */
		@Override
		public void errorHand(String errorMsg, String url) {
			Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
		}

		/**
		 * 下载线程开始前执行，不能更新UI进程，此函数实在线程内执行的。
		 */
		@Override
		public void beforeThread(String url) {

		}

		/**
		 * 在下载线程执行即将返回时执行，不能更新UI进程。在这里下载完成后调用线程做解压工作。
		 */
		@Override
		public void afterThread(Boolean result, String url, String filePath) {
			if (result) {
				try {
					InputStream inputStream = new FileInputStream(filePath);
					String outputDirectory = Environment
							.getExternalStorageDirectory()
							+ "/"
							+ Constants.SAVE_DIRECTORY + "/";
					new UnzipFile(unzipHandler, inputStream, outputDirectory,
							true).unzip();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					status = false;
				}
			}
		}

		/**
		 * 解压处理Handler，只用记录出错状况
		 */
		@SuppressLint("HandlerLeak")
		private Handler unzipHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == Constants.UNZIP_ERROR) {
					status = false;
				}
			}
		};

		/**
		 * 下载数据的更新，没10%一更新，防止出现卡顿
		 */
		@SuppressLint("DefaultLocale")
		@Override
		public void update(String url, int fileDownloaded, int fileSize) {
			String downloadedText;
			if (fileDownloaded < 1048576) {
				downloadedText = new DecimalFormat("#.00")
						.format(fileDownloaded / 1024.0) + "KB";
			} else {
				downloadedText = new DecimalFormat("#.00")
						.format(fileDownloaded / 1048576.0) + "MB";
			}
			String totalText;
			if (fileSize < 1048576) {
				totalText = new DecimalFormat("#.00").format(fileSize / 1024.0)
						+ "KB";
			} else {
				totalText = new DecimalFormat("#.00")
						.format(fileSize / 1048576.0) + "MB";
			}
			String progressText = String.format("%d%%(%s/%s)",
					(int) (((double) fileDownloaded / fileSize) * 100),
					downloadedText, totalText);

			notification.contentView.setTextViewText(
					R.id.txt_download_progress, progressText);
			notificationManager.notify(notificationId, notification);
		}
		/**
		 * addAnAbort 让一个地址的下载取消
		 * 
		 * @param url
		 *            想要取消下载的地址
		 */
		public void addAnAbort(String url) {
			OnlineListCursorAdapter.downloadingNotificationCancels.add(url);
		}
		/**
		 * 是否取消了下载，如果取消了，则从下载列表中删除
		 */
		@Override
		public boolean ifAbort(String url) {
			return OnlineListCursorAdapter.downloadingNotificationCancels
					.remove(url);
		}
	}

}
