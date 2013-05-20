package com.zhan_dui.dictionary.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * 获取Json的工具类
 * 
 * @author xuanqinanhai
 * 
 */
public class JsonGetter {
	private JsonGetter() {

	}

	/**
	 * 获取网页json数据;
	 * 
	 * @param JsonUrl
	 *            json网页地址
	 * @return 返回Json数据
	 * @throws Exception
	 */
	public static String get(String JsonUrl) throws Exception {
		URL url = new URL(JsonUrl);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setConnectTimeout(30000);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(urlConnection.getInputStream()));
		StringBuilder jsonResultBuilder = new StringBuilder();
		String line = "";
		while ((line = bufferedReader.readLine()) != null) {
			jsonResultBuilder.append(line);
		}
		return jsonResultBuilder.toString();
	}
}
