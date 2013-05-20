package com.zhan_dui.dictionary.adapters;

import com.zhan_dui.dictionary.R;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OfflineDictionaryCursorAdapter extends CursorAdapter {

	private LayoutInflater layoutInflater;
	public OfflineDictionaryCursorAdapter(Context context, Cursor c,
			boolean autoRequery) {
		super(context, c, autoRequery);
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		WordViewHolder wordViewHolder;
		if (convertView.getTag() == null) {
			wordViewHolder = new WordViewHolder();
			wordViewHolder.word = (TextView) convertView
					.findViewById(R.id.new_word);
			convertView.setTag(wordViewHolder);
		} else {
			wordViewHolder = (WordViewHolder) convertView.getTag();
		}
		wordViewHolder.word.setText(cursor.getString(cursor
				.getColumnIndex("word")));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup root) {
		View word_view = layoutInflater.inflate(R.layout.word_item, null);
		return word_view;
	}

	private class WordViewHolder {
		TextView word;
	}

}
