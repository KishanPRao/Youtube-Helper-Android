package com.kishanprao.youtubehelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kishan P Rao on 14/08/17.
 */

public class YouTubeWebView extends WebView {
	interface YouTubeStateCallback {
		void onVideoLoaded(int duration);
		
		void onTimeUpdated(int time);
	}
	
	private class JSCallback {
		
		@JavascriptInterface
		public void setDuration(String duration) {
			if (VERBOSE) Log.v(TAG, "setDuration, " + duration);
			if (mStateCallback != null) {
				mStateCallback.onVideoLoaded((int) Float.parseFloat(duration));
			}
			startSeekCallback();
		}
		
		@JavascriptInterface
		public void onSeekUpdated(String time) {
			if (mStateCallback != null) {
				mStateCallback.onTimeUpdated((int) Float.parseFloat(time));
			}
		}
	}
	
	private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (Macintosh; " +
			"U; Intel Mac OS X 10_5_5; en-us) AppleWebKit/525.18 (KHTML, " +
			"like Gecko) Version/3.1.2 Safari/525.20.1";
	private static final boolean VERBOSE = false;
	private static final String TAG = YouTubeWebView.class.getSimpleName();
	private static final int UPDATE_DELAY = 500;
	
	private YouTubeSpeed mCurrentSpeed = YouTubeSpeed.FAST;
	private YouTubeQuality mCurrentQuality = YouTubeQuality.Q360;
	private YouTubeStateCallback mStateCallback = null;
	private Timer mTimer;
	
	public YouTubeWebView(Context context) {
		this(context, null);
	}
	
	public YouTubeWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}
	
	public YouTubeSpeed getPlaybackSpeed() {
		return mCurrentSpeed;
	}
	
//	Unused for now, need proper UI.
	public YouTubeQuality getPlaybackQuality() {
		return mCurrentQuality;
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	private void initialize() {
		setLongClickable(true);
		setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});
		getSettings().setMediaPlaybackRequiresUserGesture(false);
		getSettings().setJavaScriptEnabled(true);
		getSettings().setUserAgentString(DESKTOP_USER_AGENT);
		getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
		setWebChromeClient(
				new WebChromeClient() {
					@Override
					public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
						if (VERBOSE) Log.v(TAG, "onConsoleMessage, " + consoleMessage.message());
						return super.onConsoleMessage(consoleMessage);
					}
				}
		
		);
		addJavascriptInterface(new JSCallback(), "Android");
	}
	
	public void setCallback(YouTubeStateCallback callback) {
		this.mStateCallback = callback;
	}
	
	public void loadPlayer(String url) {
		int width = getResources().getInteger(R.integer.player_full_width);
		int height = getResources().getInteger(R.integer.player_full_height);
		YouTubeJS.loadPlayer(width, height, url, mCurrentSpeed, mCurrentQuality, this);
	}
	
	public void startSeekCallback() {
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				YouTubeJS.updateSeek(YouTubeWebView.this);
			}
		}, UPDATE_DELAY, UPDATE_DELAY);
	}
	
	public void stopSeekCallback() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}
	
	public void seekTo(int time) {
		YouTubeJS.seekTo(time, this);
	}
	
	public void setPlaybackSpeed(YouTubeSpeed speed) {
		YouTubeJS.setPlaybackSpeed(speed, this);
	}
	
	public void pause() {
		pauseTimers();
		onPause();
	}
	
	public void resume() {
		resumeTimers();
		onResume();
	}
	
	public void release() {
		stopSeekCallback();
		removeAllViews();
		destroy();
	}
}
