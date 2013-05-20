package com.zhan_dui.dictionary.utils;

public class Constants {
	public static final String ONLINE_DICTIONARY_LIST_URL = "http://dic.zhan-dui.com/json.php";

	public static final int DOWNLOADING = 1;
	public static final int DOWNLOAD_ERROR = -1;
	public static final int DOWNLOAD_SUCCESS = 0;
	public static final int DOWNLOAD_FINISH = 2;
	public static final int DOWNLOAD_CANCEL = 3;
	public static final int DOWNLOAD_START = 4;

	public static final int MOVE_START = 0;
	public static final int MOVING = 1;
	public static final int MOVE_END = 2;
	public static final int MOVE_ERROR = 3;

	public static final int CONNECTION_ERROR = 6;

	public static final int FILE_CREATE_ERROR = 4;
	public static final int MALFORM_URL = 5;

	public static final String PREF_FIRST = "FIRST_START";
	/**
	 * 存储数据的目录
	 */
	public static final String SAVE_DIRECTORY = "dictionary";
	public static final String BASE_DICTIONARY = "dictionary_word.sqlite";
	public static final String BASE_DICTIONARY_ASSET = "dictionary_word.sqlite.zip";

	public static final int UNZIPPING = 0;
	public static final int UNZIP_ERROR = -1;
	public static final int UNZIP_START = 1;
	public static final int UNZIP_FINISH = 2;

	public static final int DEFAULT_SMALL_SIZE = 14;
	public static final int DEFAULT_MEDIUM_SIZE = 19;
	public static final int DEFAULT_LARGE_SIZE = 25;

}
