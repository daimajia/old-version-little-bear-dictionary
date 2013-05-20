package com.zhan_dui.dictionary.filemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.zhan_dui.dictionary.R;
import com.zhan_dui.dictionary.db.DictionaryDB;
import com.zhan_dui.dictionary.utils.UnzipUtils;
import com.zhan_dui.dictionary.utils.UnzipUtils.UnzipInterface;

/**
 * ����������еĽ�ѹ,ά���ڽ�ѹʱ������Notification �÷����ȴ���һ��UnzipNotificationCenter
 * ���������Ϊ���Notificationʱ������Class,�������PrepareNotification,���ص�ǰ�����Notification��ID��
 * ���startUnzip ����һ��NotificationID
 * 
 * @date 2012-11-29 ����9:23:52
 */
public class UnzipNotificationCenter {

	private static HashMap<String, Integer> unzippingMap = new HashMap<String, Integer>();
	@SuppressLint("UseSparseArrays")
	private static HashMap<Integer, Notification> unzippingNotificationMap = new HashMap<Integer, Notification>();

	private static int idCounter = 0;
	private Context context;
	private NotificationManager notificationManager;
	private PendingIntent pendingIntent;
	private UnzipUtils unzipUtils;
	/**
	 * ����ѹ֪ͨ�����ġ����캯��
	 * 
	 * @param context
	 *            ��ǰ��������
	 * @param cls
	 *            ���֪ͨ������������
	 */
	public UnzipNotificationCenter(Context context, Class<?> cls) {
		this.context = context;

		this.pendingIntent = PendingIntent.getActivity(context, 0, new Intent(
				context, cls), Intent.FLAG_ACTIVITY_CLEAR_TOP);

		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		unzipUtils = new UnzipUtils();
	}
	/**
	 * prepareUnzipNotification ׼��һ��Notification
	 * 
	 * @param icon
	 *            ֪ͨ��ͼ��
	 * @param tickerText
	 *            ֪ͨ��tikerText
	 * @param contentTitle
	 *            ֪ͨ��title
	 * @param content
	 *            ֪ͨ������
	 * @return int ���ش��������notification��id ��һ��ͨ��id������һ����ѹ����
	 */
	public int prepareUnzipNotification(int icon, String tickerText,
			String contentTitle, String content) {
		Notification notification = new Notification(icon, tickerText,
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(context, contentTitle, content,
				pendingIntent);
		unzippingNotificationMap.put(idCounter, notification);
		return idCounter++;
	}

	public int prepareUnzipNotification(int icon, int tickerTextResId,
			int contentTitleResId, int contentResId) {
		return prepareUnzipNotification(icon,
				context.getString(tickerTextResId),
				context.getString(contentTitleResId),
				context.getString(contentTitleResId));
	}

	/**
	 * startUnzip ����һ�����ع���
	 * 
	 * @param notificationId
	 *            ͨ��prepareUnzipNotification������Notication��id
	 * @param source
	 *            ��ѹ�ļ���ַ
	 * @param outputDirectory
	 *            ����ļ���
	 * @param rewrite
	 *            �Ƿ񸲸�
	 */
	public void startUnzip(int notificationId, String source,
			String outputDirectory, Boolean rewrite) {
		if (unzippingNotificationMap.containsKey(notificationId) == false) {
			return;
		} else {
			if (unzippingMap.containsKey(source)) {// ����Ƿ����ظ��ļ�����
				Toast.makeText(context, R.string.unzip_already,
						Toast.LENGTH_SHORT).show();
				unzippingNotificationMap.remove(notificationId);
				return;
			}
			notificationManager.notify(notificationId,
					unzippingNotificationMap.get(notificationId));
			unzippingMap.put(source, notificationId);
			unzipUtils.unzipFile(unzipBehavior, source, outputDirectory,
					rewrite);
		}
	}
	private UnzipUtils.UnzipInterface unzipBehavior = new UnzipInterface() {

		@Override
		public boolean beforeUnzip(String source, String outputDirectory) {
			int lastSep = source.lastIndexOf("/");
			String fileName = source.substring(lastSep + 1, source.length());
			// ����notification����֪ͨ��Ϊ ��ʼ��ѹ
			String title = context.getString(R.string.start_unzip) + fileName;
			String content = context.getString(R.string.unzip_tip);
			updateNotification(source, title, content);
			return true;
		}

		@Override
		public void afterUnzip(Boolean result, String source,
				String outputDirectory) {
			// ����notification����֪ͨ��Ϊ ��ѹ����
			updateNotification(source, R.string.unzip_finish,
					R.string.unzip_tip);
			// ���ҴӴ洢�б���ɾ��
			removeFromListsByPath(source);
		}

		@Override
		public void beforeUnzipThread(String source, String outputDirectory) {
		}
		/**
		 * ��ѹ�̼߳�������ʱִ�е�����
		 */
		@Override
		public void afterUnzipThread(Boolean result, String source,
				String outputDirectory) {
			// ������ѹ������ݣ�������ӵ����ݿ���
			updateNotification(source, R.string.unzip_finish_start_deal,
					R.string.still_wait);
			// ��ʼ�����ݿ��е�����ֵ����Ϣ
			File currentFile = new File(source);
			String fileName = currentFile.getName();
			String onlyName = fileName.substring(0, fileName.lastIndexOf("."));
			File importFile = new File(outputDirectory + "/import-" + onlyName
					+ ".dic");
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(
						new FileInputStream(importFile));
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String queryString = bufferedReader.readLine();
				DictionaryDB dictionaryDB = new DictionaryDB(context,
						DictionaryDB.DB_NAME, null, DictionaryDB.DB_VERSION);
				SQLiteDatabase sqLiteDatabase = dictionaryDB
						.getWritableDatabase();
				Log.i("queryString", queryString);
				sqLiteDatabase.execSQL(queryString);
				sqLiteDatabase.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/**
		 * ������
		 */
		@Override
		public void errorOccur(String errorMsg, String source,
				String outputDirectory) {
			Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
		}

	};
	/**
	 * updateNotification ����notification
	 * 
	 * @param source
	 *            Դ�ļ���ַ�����������Ѿ��洢��notification
	 * @param title
	 *            ����notification title
	 * @param content
	 *            ����notification content
	 */
	private void updateNotification(String source, String title, String content) {
		Notification notification = getNotificationByPath(source);
		if (notification == null)
			return;
		notification.setLatestEventInfo(context, title, content, pendingIntent);
		notificationManager.notify(unzippingMap.get(source), notification);
	}
	/**
	 * updateNotification ����Notification
	 * 
	 * @param source
	 *            Դ�ļ���ַ
	 * @param strTitleRes
	 *            ����title����ԴID
	 * @param strContentRes
	 *            �������ݵ���ԴID
	 */
	private void updateNotification(String source, int strTitleRes,
			int strContentRes) {
		updateNotification(source, context.getString(strTitleRes),
				context.getString(strContentRes));
	}
	/**
	 * getNotificationByPath ͨ��Ҫ��ѹ���ļ�
	 * 
	 * @param source
	 *            �ļ���ַ
	 * @return ����notification
	 */
	private Notification getNotificationByPath(String source) {
		if (unzippingMap.containsKey(source)) {
			int id = unzippingMap.get(source);
			return unzippingNotificationMap.get(id);
		} else {
			return null;
		}
	}

	private void removeFromListsByPath(String source) {
		if (unzippingMap.containsKey(source)) {
			int id = unzippingMap.get(source);
			notificationManager.cancel(id);
			unzippingNotificationMap.remove(id);
			unzippingMap.remove(source);
		}
	}
}
