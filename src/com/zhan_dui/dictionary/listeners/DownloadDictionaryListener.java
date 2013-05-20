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
 * ���ذ�ť�����������º���������߳�������ͬʱ��������Notification
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
		// ʹ���Զ���������ͼʱ������Ҫ�ٵ���setLatestEventInfo()����
		// ���Ǳ��붨�� contentIntent
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
	 * @Description:���ع����е���Ϊ������ʵ����DownloadUtilsInterface
	 * @date 2012-11-28 ����2:51:50
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
		 * ����֮ǰִ�е���Ϊ����Ҫ���޸�����״̬�б�
		 */
		@Override
		public void beforeDownload(String url) {
			OnlineListCursorAdapter.downloadingNotificationUrls.add(url);
			currentAdapter.notifyDataSetChanged();
		}
		/**
		 * ���ؽ���ʱ�����Ϊ����Ҫ�ǽ����غõ��ֵ���ӵ����ݿ��С�
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
		 * ������������س�����ִ�У�����һ���ǳ�����Ϣ������ֱ����ʾ��һ���ǳ������������
		 */
		@Override
		public void errorHand(String errorMsg, String url) {
			Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
		}

		/**
		 * �����߳̿�ʼǰִ�У����ܸ���UI���̣��˺���ʵ���߳���ִ�еġ�
		 */
		@Override
		public void beforeThread(String url) {

		}

		/**
		 * �������߳�ִ�м�������ʱִ�У����ܸ���UI���̡�������������ɺ�����߳�����ѹ������
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
		 * ��ѹ����Handler��ֻ�ü�¼����״��
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
		 * �������ݵĸ��£�û10%һ���£���ֹ���ֿ���
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
		 * addAnAbort ��һ����ַ������ȡ��
		 * 
		 * @param url
		 *            ��Ҫȡ�����صĵ�ַ
		 */
		public void addAnAbort(String url) {
			OnlineListCursorAdapter.downloadingNotificationCancels.add(url);
		}
		/**
		 * �Ƿ�ȡ�������أ����ȡ���ˣ���������б���ɾ��
		 */
		@Override
		public boolean ifAbort(String url) {
			return OnlineListCursorAdapter.downloadingNotificationCancels
					.remove(url);
		}
	}

}
