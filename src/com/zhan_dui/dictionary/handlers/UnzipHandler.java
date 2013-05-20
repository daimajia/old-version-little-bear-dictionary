package com.zhan_dui.dictionary.handlers;

import com.zhan_dui.dictionary.utils.Constants;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
/**
 * @Description:解压文件时候的显示数据handler
 * @date 2012-11-9 上午11:21:01
 */
public class UnzipHandler extends Handler {

	private Context context;
	private ProgressDialog progressDialog = null;

	public UnzipHandler(Context context) {
		this.context = context;
	}

	/**
	 * what 代表状态 arg1 代表进度 arg2 代表总大小
	 */

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (msg.what == Constants.UNZIPPING) {
			progressDialog.setMessage("正在初始化移动，请稍后"
					+ String.valueOf(msg.arg1 / 1024) + "KB");
		} else if (msg.what == Constants.UNZIP_START) {
			progressDialog = new ProgressDialog(context);
			progressDialog.show();
			progressDialog.setTitle("Unzipping");
		} else if (msg.what == Constants.UNZIP_ERROR) {
			Toast.makeText(context, "Unzip error", Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
		} else if (msg.what == Constants.UNZIP_FINISH) {
			progressDialog.dismiss();
		}
	}
}
