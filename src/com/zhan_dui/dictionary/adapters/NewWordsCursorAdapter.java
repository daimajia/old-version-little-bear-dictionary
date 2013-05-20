package com.zhan_dui.dictionary.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.zhan_dui.dictionary.R;

public class NewWordsCursorAdapter extends CursorAdapter {

	private LayoutInflater layoutInflater;
	private OnClickListener buttonClickListener;

	private NewWordsCursorAdapter(Context context, Cursor c, boolean autoQuery) {
		super(context, c, autoQuery);
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public NewWordsCursorAdapter(Context context, Cursor cursor,
			boolean autoQuery, OnClickListener buttOnClickListener) {
		this(context, cursor, autoQuery);
		this.buttonClickListener = buttOnClickListener;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		WordViewHolder wordViewHolder;
		if (convertView.getTag() == null) {
			wordViewHolder = new WordViewHolder();
			wordViewHolder.word = (Button) convertView
					.findViewById(R.id.new_word);
			convertView.setTag(wordViewHolder);
		} else {
			wordViewHolder = (WordViewHolder) convertView.getTag();
		}
		String word = cursor.getString(cursor.getColumnIndex("word"));
		wordViewHolder.word.setText(word);
		wordViewHolder.word.setContentDescription(word);
		wordViewHolder.word.setOnClickListener(buttonClickListener);

	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup root) {
		View word_view = layoutInflater.inflate(R.layout.grid_view_item, null);
		return word_view;
	}

	// private class WordViewHolder {
	// TextView word;
	// }
	private class WordViewHolder {
		Button word;
	}

}
