package com.kishanprao.youtubehelper;

import android.content.Context;

import com.kishanprao.youtubehelper.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Kishan P Rao on 15/08/17.
 */

public class Utils {
	private static final boolean VERBOSE = BuildConfig.DEBUG;
	public static String loadAsset(String fileName, Context context) {
		BufferedReader reader = null;
		String data = "";
		try {
			reader = new BufferedReader(
					new InputStreamReader(context.getAssets().open(fileName), "UTF-8"));
			String mLine;
			while ((mLine = reader.readLine()) != null) {
				data += mLine;
			}
		} catch (IOException e) {
			if (VERBOSE) e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					if (VERBOSE) e.printStackTrace();
				}
			}
		}
		return data;
	}
}
