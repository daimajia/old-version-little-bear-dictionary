package com.zhan_dui.dictionary.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UnzipFileInThread extends Thread {

	private Handler dealHandler;
	private InputStream inputStream;
	private String outputDirectory;
	private boolean isReWrite;

	public UnzipFileInThread(Handler dealHandler, InputStream inputStream,
			String outputDirectory, boolean isReWrite) {
		super();
		this.dealHandler = dealHandler;
		this.inputStream = inputStream;
		this.outputDirectory = outputDirectory;
		this.isReWrite = isReWrite;
		this.dealHandler = dealHandler;
	}

	/**
	 * ��ѹassets��zipѹ���ļ���ָ��Ŀ¼
	 * 
	 * @param context�����Ķ���
	 * @param assetNameѹ���ļ���
	 * @param outputDirectory���Ŀ¼
	 * @param isReWrite�Ƿ񸲸�
	 * @throws IOException
	 */

	@Override
	public void run() {
		super.run();
		// ������ѹĿ��Ŀ¼
		File file = new File(outputDirectory);
		// ���Ŀ��Ŀ¼�����ڣ��򴴽�
		if (!file.exists()) {
			file.mkdirs();
		}
		// ��ѹ���ļ�
		ZipInputStream zipInputStream = new ZipInputStream(inputStream);
		try {
			// ��ȡһ�������
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			// ʹ��1Mbuffer
			byte[] buffer = new byte[1024];
			// ��ѹʱ�ֽڼ���
			int count = 0;

			Message startMsg = Message.obtain(dealHandler,
					Constants.UNZIP_START);
			startMsg.sendToTarget();

			// ��������Ϊ��˵���Ѿ�����������ѹ�������ļ���Ŀ¼
			while (zipEntry != null) {
				// �����һ��Ŀ¼
				if (zipEntry.isDirectory()) {
					file = new File(outputDirectory + File.separator
							+ zipEntry.getName());
					// �ļ���Ҫ���ǻ������ļ�������
					if (isReWrite || !file.exists()) {
						file.mkdir();
					}
				} else {
					// ������ļ�
					file = new File(outputDirectory + File.separator
							+ zipEntry.getName());
					// �ļ���Ҫ���ǻ����ļ������ڣ����ѹ�ļ�
					if (isReWrite || !file.exists()) {

						file.createNewFile();
						FileOutputStream fileOutputStream = new FileOutputStream(
								file);
						int counter = 0;
						long size = zipEntry.getSize();
						while ((count = zipInputStream.read(buffer)) > 0) {
							Log.i("Count", String.valueOf(count));
							fileOutputStream.write(buffer, 0, count);
							counter += count;
							Message msg = Message.obtain(dealHandler,
									Constants.UNZIPPING, counter, (int) size);
							msg.sendToTarget();
						}
						fileOutputStream.close();
					}
				}
				// ��λ����һ���ļ����
				zipEntry = zipInputStream.getNextEntry();
			}
			zipInputStream.close();
			dealHandler.sendEmptyMessage(Constants.UNZIP_FINISH);
		} catch (Exception e) {
			dealHandler.sendEmptyMessage(Constants.UNZIP_ERROR);
			e.printStackTrace();
		}
	}
}
