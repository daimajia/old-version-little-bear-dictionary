package com.zhan_dui.dictionary.listeners;

import com.zhan_dui.dictionary.adapters.OnlineListCursorAdapter;

import android.widget.CursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @Description:取消下载监听器
 * @date 2012-11-28 下午3:21:21
 */
public class DownloadDictionaryCancelListener implements OnClickListener {

	private String toCancelUrl;
	private CursorAdapter cursorAdapter;
	public DownloadDictionaryCancelListener(String url,
			CursorAdapter cursorAdapter) {
		this.toCancelUrl = url;
		this.cursorAdapter = cursorAdapter;
	}

	@Override
	public void onClick(View v) {
		OnlineListCursorAdapter.downloadingNotificationUrls.remove(v
				.getContentDescription());
		cursorAdapter.notifyDataSetChanged();
		OnlineListCursorAdapter.downloadingNotificationCancels
				.add(this.toCancelUrl);
	}

}
