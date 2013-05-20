package com.zhan_dui.dictionary.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.AsyncTask;

public class UnzipUtils {

	public interface UnzipInterface {
		/**
		 * beforeUnzip ��Unzip֮ǰ��������
		 * 
		 * @param source
		 *            Դ�ļ���ַ
		 * @param outputDirectory
		 *            ����ѹĿ¼
		 */
		public boolean beforeUnzip(String source, String outputDirectory);
		/**
		 * afterUnzip unzip����ʱִ�еĺ��� UI�߳�
		 * 
		 * @param result
		 *            ��ѹ��� trueΪ�ɹ���falseΪʧ��
		 * @param source
		 *            Դ�ļ���ַ
		 * @param outputDirectory
		 *            ��ѹĿ���ļ��е�ַ
		 */
		public void afterUnzip(Boolean result, String source,
				String outputDirectory);
		/**
		 * errorOccur ����ʱ���øú��� UI�߳�
		 * 
		 * @param errorMsg
		 *            ������Ϣ
		 * @param source
		 *            Դ�ļ���ַ
		 * @param outputDirectory
		 *            ��ѹĿ���ļ��е�ַ
		 */

		/**
		 * beforeUnzipThread �߳̿�ʼǰ �̼߳��𣬲�Ҫ��UI����
		 */
		public void beforeUnzipThread(String source, String outputDirectory);

		/**
		 * afterUnzipThread �߳̽���ǰ �̼߳��𣬲�Ҫ��UI����
		 * 
		 * @param result
		 *            �Ƿ����
		 * @param source
		 *            Դ��ַ
		 * @param outputDirectory
		 *            ��ѹĿ���ַ
		 */
		public void afterUnzipThread(Boolean result, String source,
				String outputDirectory);

		public void errorOccur(String errorMsg, String source,
				String outputDirectory);
		/**
		 * onZipStart �ڽ�ѹ��ʼʱ���� UI�߳�
		 * 
		 * @param source
		 * @param outputDirectory
		 */

	}
	public static final String CREATE_DIRECTORY_ERROR = "�޷�������ѹ�ļ���";
	public static final String FILE_NOT_EXSIT = "����ѹ���ļ�������";
	public static final String ZIP_POINT_ERROR = "Ŀ���ļ���ַ����ѹ���ļ������Ѿ�����";
	public static final String ZIP_OUTPUTSTREAM_ERROR = "�޷�����Ҫ��ѹ��Ŀ���ļ�";
	public static final String FILESTREAM_CANNOT_CLOSE = "�ļ����޷��ر�";
	public static final String ZIP_NEXT_POINT_ERROR = "�޷���λ����һ����ѹ��";
	public static final String ABORT = "�Ѿ����û�ȡ��";
	public static final String BEFORE_TASK_STOP = "before���������˽�ѹ";

	public void unzipFile(UnzipInterface unzipBehavior, String source,
			String outputDirectory, Boolean rewrite) {
		new UnzipTask(source, outputDirectory, unzipBehavior, rewrite)
				.execute();
	}

	private class UnzipTask extends AsyncTask<Void, Integer, Boolean> {

		private String source;
		private String outputDirectory;
		private UnzipInterface unzipBehavior;
		private Boolean rewrite;
		private String errorMsg = null;
		private boolean stop = false;

		public UnzipTask(String source, String outputDirectory,
				UnzipInterface unzipBehavior, Boolean rewrite) {
			super();
			this.source = source;
			this.outputDirectory = outputDirectory;
			this.unzipBehavior = unzipBehavior;
			this.rewrite = rewrite;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// ���before�Ĺ�������false�򲻿�ʼ��ѹ
			stop = !unzipBehavior.beforeUnzip(source, outputDirectory);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			if (stop) {
				errorMsg = BEFORE_TASK_STOP;
				return false;
			}

			unzipBehavior.beforeUnzipThread(source, outputDirectory);
			// ������ѹĿ��Ŀ¼

			File file = new File(outputDirectory);
			// ���Ŀ��Ŀ¼�����ڣ��򴴽�
			if (!file.exists()) {
				if (file.mkdirs() == false) {
					errorMsg = CREATE_DIRECTORY_ERROR;
					unzipBehavior.afterUnzipThread(false, source,
							outputDirectory);
					return false;
				}
			}
			InputStream inputStream;
			try {
				inputStream = new FileInputStream(new File(source));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				errorMsg = FILE_NOT_EXSIT;
				unzipBehavior.afterUnzipThread(false, source, outputDirectory);
				return false;
			}
			// ��ѹ���ļ�
			ZipInputStream zipInputStream = new ZipInputStream(inputStream);

			ZipEntry zipEntry = null;
			try {
				zipEntry = zipInputStream.getNextEntry();
			} catch (IOException e) {
				e.printStackTrace();
				errorMsg = ZIP_POINT_ERROR;
				unzipBehavior.afterUnzipThread(false, source, outputDirectory);
				return false;
			}
			byte[] buffer = new byte[1024];
			// ��ѹʱ�ֽڼ���
			int count = 0;
			// ��������Ϊ��˵���Ѿ�����������ѹ�������ļ���Ŀ¼
			while (zipEntry != null) {
				// �����һ��Ŀ¼
				if (zipEntry.isDirectory()) {
					file = new File(outputDirectory + File.separator
							+ zipEntry.getName());
					// �ļ���Ҫ���ǻ������ļ�������
					if (rewrite || !file.exists()) {
						file.mkdir();
					}
				} else {
					// ������ļ�
					file = new File(outputDirectory + File.separator
							+ zipEntry.getName());
					// �ļ���Ҫ���ǻ����ļ������ڣ����ѹ�ļ�
					FileOutputStream fileOutputStream = null;
					if (rewrite || !file.exists()) {

						try {
							file.createNewFile();
							fileOutputStream = new FileOutputStream(file);
							while ((count = zipInputStream.read(buffer)) > 0) {
								fileOutputStream.write(buffer, 0, count);
								if (stop) {
									errorMsg = ABORT;
									return false;
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
							errorMsg = ZIP_OUTPUTSTREAM_ERROR;
							unzipBehavior.afterUnzipThread(false, source,
									outputDirectory);
							return false;
						}
						try {
							fileOutputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
							errorMsg = FILESTREAM_CANNOT_CLOSE;
							unzipBehavior.afterUnzipThread(false, source,
									outputDirectory);
							return false;
						}
					}
				}
				// ��λ����һ���ļ����
				try {
					zipEntry = zipInputStream.getNextEntry();
				} catch (IOException e) {
					e.printStackTrace();
					errorMsg = ZIP_NEXT_POINT_ERROR;
					unzipBehavior.afterUnzipThread(false, source,
							outputDirectory);
					return false;
				}
			}
			unzipBehavior.afterUnzipThread(true, source, outputDirectory);
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result == false) {
				unzipBehavior.errorOccur(errorMsg, source, outputDirectory);
			}
			unzipBehavior.afterUnzip(result, source, outputDirectory);
		}
	}
}
