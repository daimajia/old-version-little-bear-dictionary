package com.zhan_dui.dictionary.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * ��ȡJson�Ĺ�����
 * 
 * @author xuanqinanhai
 * 
 */
public class JsonGetter {
	private JsonGetter() {

	}

	/**
	 * ��ȡ��ҳjson����;
	 * 
	 * @param JsonUrl
	 *            json��ҳ��ַ
	 * @return ����Json����
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
