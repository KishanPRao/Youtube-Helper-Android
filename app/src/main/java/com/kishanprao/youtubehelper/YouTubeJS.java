package com.kishanprao.youtubehelper;

import android.util.Log;
import android.webkit.WebView;

import java.util.Locale;

/**
 * Created by Kishan P Rao on 15/08/17.
 */

class YouTubeJS {
	private static final String TAG = YouTubeJS.class.getSimpleName();
	private static final boolean VERBOSE = false;
	private static final String PARAMS = "<script>\n" +
			"var width = %d;" +
			"var height = %d;" +
			"var url = '%s';" +
			"var speed = %f;" +
			"var quality = '%s';" +
			"</script>";
	
	private static String createParams(int width, int height, String url, YouTubeSpeed initialSpeed, YouTubeQuality initialQuality) {
		return String.format(Locale.getDefault(), PARAMS, width, height, url, initialSpeed.getSpeed(), initialQuality.getQuality());
	}
	
	static void loadPlayer(int width, int height, String url, YouTubeSpeed initialSpeed, YouTubeQuality initialQuality, WebView webView) {
		String data;
		data = createParams(width, height, url, initialSpeed, initialQuality);
		if (VERBOSE) Log.v(TAG, "loadPlayer, params:" + data);
		data += Utils.loadAsset("ytube_outer.html", webView.getContext());
		webView.loadDataWithBaseURL("", data, "text/html", "UTF-8", null);
	}
	
	static void seekTo(int time, WebView webView) {
		webView.loadUrl("javascript:seekTo(" + time + ")");
	}
	
	static void setPlaybackSpeed(YouTubeSpeed speed, WebView webView) {
		webView.loadUrl("javascript:setPlaybackRate(" + speed.getSpeed() + ")");
	}
	
	static void updateSeek(final WebView webView) {
		webView.post(new Runnable() {
			@Override
			public void run() {
				webView.loadUrl("javascript:updateSeek()");
			}
		});
	}
}
