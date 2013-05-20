package com.zhan_dui.dictionary.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ProgressBar;

import com.zhan_dui.dictionary.exceptions.SDCardUnmountedException;

public class DownloadUtils {
	private DownloadUtils() {

	}

	/**
	 * ����DownloadUtils�Ĺ��ߺ���,�ֱ������ؿ�ʼ�����س��������쳣�������
	 * 
	 * @author xuanqinanhai
	 * 
	 */
	public interface DownloadUtilsInterface {
		/**
		 * ��ִ��AsyncTask excute֮ǰִ��
		 */
		public void beforeDownload(String url);

		/**
		 * �������ʱִ��
		 * 
		 * @param result
		 *            �������صĽ��
		 */
		public void afterDownload(Boolean result, String url, String savePath);

		/**
		 * ����ִ�к�����errorMsg����������,����ֱ�����
		 * 
		 * @param errorMsg
		 */
		public void errorHand(String errorMsg, String url);

		/**
		 * ���ؽ��ȸ��£�������100��
		 * 
		 * @param url
		 *            ���ص��ļ���ַ
		 * @param fileDownladed
		 *            �ļ��Ѿ����صĴ�С
		 * @param fileSize
		 *            �ļ��ܴ�С
		 */
		public void update(String url, int fileDownladed, int fileSize);

		/**
		 * �߳�ִ�п�ʼǰ����Ϊ
		 * 
		 * @param url
		 */
		public void beforeThread(String url);

		/**
		 * �߳�ִ�м�����������Ϊ
		 * 
		 * @param result
		 *            �����ļ��Ƿ�ɹ�
		 * @param url
		 *            �ļ�URL
		 * @param filePath
		 *            �ļ�����ĵ�ַ
		 */
		public void afterThread(Boolean result, String url, String filePath);

		/**
		 * ���߳�����ʱ����ã�ע�⣬��Ҫִ��ֻ����UI�߳���ִ�е���Ϊ,��ú�����Խ��Խ�ã������Ͷ������ٶȵ�Ӱ���С
		 * 
		 * @param url
		 *            Ҫ��ֹ���ص��ļ�URL
		 * @return �����Ƿ���ֹ����
		 */
		public boolean ifAbort(String url);
	}

	public static final String ERROR_CREATE_FILE = "�����ļ�����";
	public static final String ERROR_IO = "�ļ���д�쳣";
	public static final String ERROR_WRONG_URL = "�����URL��ʽ";
	public static final String ERROR_OPEN_URL = "��URL���ӳ���";
	public static final String ERROR_SD_CARD = "δ��⵽SD��";
	public static final String ERROR_CANCEL_DOWNLOAD = "����ֹ������";

	private static class DownloadAsync
			extends
				AsyncTask<Void, Integer, Boolean> {

		String fileUrl, savePath;
		DownloadUtils.DownloadUtilsInterface downloadBehavior;
		String errorMsg;
		int size = 0;// �ļ���С
		double downloaded = 0.0;
		NotificationManager notificationManager;
		Notification notification;
		int progressbarId;
		int notificationId;
		boolean isUpdateNotification = false;
		ProgressBar progressBar;

		public DownloadAsync(String fileUrl, String savePath,
				DownloadUtilsInterface downloadBehavior, ProgressBar progressBar) {
			this.fileUrl = fileUrl;
			this.savePath = savePath;
			this.downloadBehavior = downloadBehavior;
			this.progressBar = progressBar;
		}

		public DownloadAsync(String fileUrl, String savePath,
				DownloadUtilsInterface downloadBehavior,
				NotificationManager notificationManager,
				Notification notification, int progressbarId, int notificationId) {
			this.fileUrl = fileUrl;
			this.savePath = savePath;
			this.downloadBehavior = downloadBehavior;
			this.notification = notification;
			this.progressbarId = progressbarId;
			this.isUpdateNotification = true;
			this.notificationId = notificationId;
			this.notificationManager = notificationManager;
		}

		protected void setFileSize(int size) {
			this.size = size;
			if (!isUpdateNotification) {
				progressBar.setMax(size);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			downloadBehavior.beforeDownload(this.fileUrl);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (isUpdateNotification) {
				notification.contentView.setProgressBar(progressbarId, 100,
						values[0], false);
				this.notificationManager.notify(notificationId, notification);
				downloadBehavior.update(fileUrl, values[1], values[2]);
			} else {
				progressBar.setProgress(values[0]);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!result)
				downloadBehavior.errorHand(errorMsg, this.fileUrl);
			downloadBehavior.afterDownload(result, this.fileUrl, this.savePath);
			if (isUpdateNotification) {
				notificationManager.cancel(notificationId);
			}
		}

		@Override
		/**
		 * ��̨�߳�����
		 */
		protected Boolean doInBackground(Void... params) {
			downloadBehavior.beforeThread(this.fileUrl);
			File file = new File(savePath);
			URL url = null;

			InputStream fileInputStream = null;
			HttpURLConnection httpURLConnection = null;
			FileOutputStream fileOutputStream = null;
			BufferedOutputStream bufferedOutputStream = null;
			try {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					throw new SDCardUnmountedException();
				}
				if (file.exists() == false) {
					file.createNewFile();
				}
				url = new URL(fileUrl);
				Log.e("fileUrl", fileUrl);
				httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setInstanceFollowRedirects(true);
				fileInputStream = httpURLConnection.getInputStream();
				setFileSize(httpURLConnection.getContentLength());
				fileOutputStream = new FileOutputStream(file);
				bufferedOutputStream = new BufferedOutputStream(
						fileOutputStream);
				byte[] dataBuffer = new byte[1024];
				int length = 0;
				int downloadedPercentage;
				int everyPieceSize = size / 100;
				int oneTimeDownloadPieceSize = 0;
				while ((length = fileInputStream.read(dataBuffer)) > 0) {
					bufferedOutputStream.write(dataBuffer, 0, length);

					oneTimeDownloadPieceSize += length;

					if (oneTimeDownloadPieceSize > everyPieceSize) {
						downloaded += oneTimeDownloadPieceSize;
						downloadedPercentage = (int) ((downloaded / size) * 100);
						onProgressUpdate(downloadedPercentage,
								(int) downloaded, size);
						oneTimeDownloadPieceSize = 0;
					}

					if (downloadBehavior.ifAbort(fileUrl)) {
						errorMsg = ERROR_CANCEL_DOWNLOAD;
						break;
					}
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
				errorMsg = ERROR_WRONG_URL;
			} catch (SDCardUnmountedException e) {
				e.printStackTrace();
				errorMsg = ERROR_SD_CARD;
			} catch (IOException e) {
				e.printStackTrace();
				errorMsg = ERROR_IO;
			} finally {
				if (fileInputStream != null) {
					try {
						fileInputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
						errorMsg = ERROR_IO;
					}
				}
				if (bufferedOutputStream != null) {
					try {
						bufferedOutputStream.flush();
						bufferedOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
						errorMsg = ERROR_IO;
					}
				}
			}
			downloadBehavior.afterThread(errorMsg == null, fileUrl, savePath);
			return errorMsg == null;
		}
	}

	/**
	 * �����ļ������½�����
	 * 
	 * @param fileUrl
	 *            �ļ������ַ
	 * @param savePath
	 *            ����ĵ�ַ
	 * @param progressBar
	 *            ������������������
	 * @param downlaodInterface
	 *            ������Ϊ�ӿڣ��ֱ��������ǰ������ʱ�����غ�����Ϣ
	 */
	public static void download(String fileUrl, String savePath,
			ProgressBar progressBar, DownloadUtilsInterface downlaodInterface) {
		new DownloadAsync(fileUrl, savePath, downlaodInterface, progressBar)
				.execute();
	}

	/**
	 * �����ļ�����ͨ��notification����
	 * 
	 * @param fileUrl
	 *            �ļ���ַ
	 * @param savePath
	 *            �����ַ
	 * @param downloadBehavior
	 *            ������Ϊ�ӿڣ��ֱ��������ǰ������ʱ�����غ�����Ϣ
	 * @param notification
	 *            ֪ͨ��������������֪ͨ�����ؽ���
	 * @param progressbarId
	 *            ֪ͨ���н�����ID
	 * 
	 */
	public static void download(String fileUrl, String savePath,
			DownloadUtilsInterface downloadBehavior,
			NotificationManager notificationManager, Notification notification,
			int progressbarId, int notificationId) {
		new DownloadAsync(fileUrl, savePath, downloadBehavior,
				notificationManager, notification, progressbarId,
				notificationId).execute();
	}
}
