package com.zhan_dui.dictionary.db;

import java.util.ArrayList;

/**
 * 一个字典XML在解析时候所需要的信息。
 * 
 * @author xuanqinanhai
 * 
 */
public class DictionaryParseInfomation {

	public String title;// 字典名称
	public String table;// 查询表名
	public ArrayList<String> queryWords = new ArrayList<String>();// 查询字符串
	public ArrayList<EchoViews> echoViews = new ArrayList<DictionaryParseInfomation.EchoViews>();// 所有要输出的类型

	public class EchoViews {
		public String sprintfString;// 输出字符串格式，Sting.format
		public ArrayList<TextArg> sprintfArgs = new ArrayList<TextArg>();// 输出字符串所有的参数
		public String viewType;// View类型

		public int view_padding_left = 0, view_padding_right = 0,
				view_padding_top = 0, view_padding_bottom = 0;

		@Override
		public String toString() {
			return sprintfString + " " + sprintfArgs.toString() + " "
					+ viewType;
		}

		public void addOneArg() {
			sprintfArgs.add(new TextArg());
		}

		public TextArg getLastOneArg() {
			return sprintfArgs.get(sprintfArgs.size() - 1);
		}
	}

	public class TextArg {
		public String textSize = "normal";
		public String textColor = "#000000";
		public String action = null;
		public String argContent;
		public String textStyle = "normal";
		public int text_padding_left = 0, text_padding_right = 0,
				text_padding_top = 0, text_padding_bottom = 0;
	}

	public void addOneEchoView() {
		echoViews.add(new EchoViews());
	}

	public EchoViews getLastOneEchoView() {
		return echoViews.get(echoViews.size() - 1);
	}

	@Override
	public String toString() {
		System.out.println("title:" + title);
		System.out.println("querywords:" + queryWords.toString());
		System.out.println("echoviews:");
		for (EchoViews echoView : echoViews) {
			System.out.println(echoView.toString());
		}
		return super.toString();
	}
}
