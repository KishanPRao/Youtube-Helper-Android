package com.kishanprao.youtubehelper;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VerticalSeekBar;

public class MainActivity extends AppCompatActivity implements NetworkConnectivityListener.ConnectivityCallback, YouTubeWebView.YouTubeStateCallback {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final boolean VERBOSE = false;
	private static final long ANIM_DURATION = 300;
	
	private YouTubeWebView mWebView;
	private VerticalSeekBar mVolumeSeekBar;
	private SeekBar mPlayerSeek;
	private AudioManager mAudioManager;
	private AudioContentObserver mAudioContentObserver;
	
	private boolean mInitialized = false;
	
	//	TODO: Shared Preferences!
	private TextView mSpeed;
	private YouTubeSpeed[] mSpeeds = YouTubeSpeed.values();
	private int mCurrentSpeed = 0;
	
	//	Toggle called initially, starts with full screen.
	private boolean mFullScreen = false;
	private View mLeftControls;
	private float mInitBottomY = Float.MAX_VALUE;
	private ImageButton mToggleFullScreen;
	private View mYTRedirect;
	
	private NetworkConnectivityListener mConnectivityListener;
	private View mErrorLayout;
	private ImageView mErrorImage;
	private TextView mErrorText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (VERBOSE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
		mWebView = (YouTubeWebView) findViewById(R.id.web_view);
		mWebView.setCallback(this);
		mSpeed = (TextView) findViewById(R.id.current_speed);
		mVolumeSeekBar = (VerticalSeekBar) findViewById(R.id.volume_control);
		mPlayerSeek = (SeekBar) findViewById(R.id.player_seek);
		mLeftControls = findViewById(R.id.left_controls);
		mErrorLayout = findViewById(R.id.error_layout);
		mErrorImage = (ImageView) findViewById(R.id.error_image);
		mErrorText = (TextView) findViewById(R.id.error_text);
		
		mConnectivityListener = new NetworkConnectivityListener();
		mConnectivityListener.callback = this;
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mConnectivityListener, filter);
		
		if (mConnectivityListener.isNetworkConnected(getApplicationContext())) {
			loadPlayer();
		} else {
			noNetwork();
		}
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mVolumeSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		updateVolume();
		mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (VERBOSE) Log.v(TAG, "onProgressChanged, " + progress);
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		mAudioContentObserver = new AudioContentObserver(new Handler());
		getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mAudioContentObserver);
		
		mPlayerSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				if (mInitialized) {
					mWebView.stopSeekCallback();
				}
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mInitialized) {
					seekTo(seekBar.getProgress());
				}
			}
		});
		
		findViewById(R.id.increase_speed).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				increaseSpeed();
			}
		});
		findViewById(R.id.decrease_speed).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				decreaseSpeed();
			}
		});
		mToggleFullScreen = (ImageButton) findViewById(R.id.full_screen);
		mToggleFullScreen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleFullScreen();
			}
		});
		mYTRedirect = findViewById(R.id.youtube_redirect);
		mYTRedirect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = getPackageManager().getLaunchIntentForPackage(getString(R.string.youtube_package_name));
				if (intent == null) {
//					TODO: Handle link access.
					intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.youtube_site)));
				}
				startActivity(intent);
			}
		});
		
		mPlayerSeek.post(new Runnable() {
			@Override
			public void run() {
				toggleFullScreen();
			}
		});
		mCurrentSpeed = mWebView.getPlaybackSpeed().ordinal();
		mSpeed.setText(String.valueOf(mWebView.getPlaybackSpeed().getSpeed()));
	}
	
	private void noNetwork() {
		mErrorText.setText(R.string.error_no_internet);
		mErrorImage.setImageResource(R.drawable.ic_signal_cellular_connected_no_internet_0_bar_black_24dp);
		mErrorLayout.setVisibility(View.VISIBLE);
	}
	
	private void noVideo() {
		mErrorText.setText(R.string.error_no_video);
		mErrorImage.setImageResource(R.drawable.ic_warning_black_24dp);
		mErrorLayout.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onNetworkConnected() {
		loadPlayer();
	}
	
	private void increaseSpeed() {
		if (mInitialized) {
			if ((mCurrentSpeed + 1) < mSpeeds.length) {
				mCurrentSpeed++;
				setPlaybackSpeed(mSpeeds[mCurrentSpeed]);
			}
		}
	}
	
	private void decreaseSpeed() {
		if (mInitialized) {
			if ((mCurrentSpeed - 1) > -1) {
				mCurrentSpeed--;
				setPlaybackSpeed(mSpeeds[mCurrentSpeed]);
			}
		}
	}
	
	private void seekTo(int time) {
		mWebView.seekTo(time);
		mWebView.startSeekCallback();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.setIntent(intent);
		if (VERBOSE) Log.v(TAG, "onNewIntent:");
		loadPlayer();
	}
	
	private void setPlaybackSpeed(YouTubeSpeed speed) {
		mWebView.setPlaybackSpeed(speed);
		mSpeed.setText(String.valueOf(speed.getSpeed()));
	}
	
	private void toggleFullScreen() {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mWebView.getLayoutParams();
		mFullScreen = !mFullScreen;
		int flags;
		if (mFullScreen) {
			params.bottomMargin = 0;
			params.setMarginStart(0);
			flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				flags = flags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
			mToggleFullScreen.setImageResource(R.drawable.ic_fullscreen_exit_white_48dp);
		} else {
			int margin = getResources().getDimensionPixelSize(R.dimen.items_size);
			params.bottomMargin = margin;
			params.setMarginStart(margin);
			flags = View.SYSTEM_UI_FLAG_VISIBLE;
			mToggleFullScreen.setImageResource(R.drawable.ic_fullscreen_white_48dp);
		}
		mWebView.setLayoutParams(params);
		updateControls();
		getWindow().getDecorView().setSystemUiVisibility(flags);
	}
	
	private void updateControls() {
		if (mInitBottomY == Float.MAX_VALUE) {
			mInitBottomY = mPlayerSeek.getY();
		}
		ViewGroup.MarginLayoutParams seekParams = (ViewGroup.MarginLayoutParams) mPlayerSeek.getLayoutParams();
		ViewGroup.MarginLayoutParams ytRedirectParams = (ViewGroup.MarginLayoutParams) mYTRedirect.getLayoutParams();
		float leftX;
		float bottomY;
		if (mFullScreen) {
			leftX = -mLeftControls.getWidth();
			bottomY = mInitBottomY + mPlayerSeek.getHeight() * 0.45f;
			seekParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.side_pane_width);
			ytRedirectParams.leftMargin = 0;
			mPlayerSeek.getThumb().mutate().setAlpha(0);
		} else {
			leftX = 0;
			bottomY = mInitBottomY;
			seekParams.rightMargin = 0;
			ytRedirectParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.yt_redirect_margin_start);
			mPlayerSeek.getThumb().mutate().setAlpha(255);
		}
		mLeftControls.animate().x(leftX).setDuration(ANIM_DURATION).start();
		mPlayerSeek.animate().y(bottomY).setDuration(ANIM_DURATION).start();
		
		mPlayerSeek.setLayoutParams(seekParams);
		mYTRedirect.setLayoutParams(ytRedirectParams);
		
	}
	
	private void loadPlayer() {
		Bundle extras = getIntent().getExtras();
		String url = null;
		if (extras != null && extras.containsKey(Intent.EXTRA_TEXT)) {
			String value = extras.getString(Intent.EXTRA_TEXT);
			if (VERBOSE) Log.v(TAG, "loadPlayer, " + value);
			if (value != null) {
				url = value.substring(value.lastIndexOf('/') + 1);
			}
		}
		
		if (url != null) {
			mWebView.loadPlayer(url);
			mConnectivityListener.isLoaded = true;
			mErrorLayout.setVisibility(View.GONE);
		} else {
			Log.w(TAG, "loadPlayer, No Video to load");
			noVideo();
		}
	}
	
	private void updateVolume() {
		mVolumeSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mWebView.resume();
		if (mFullScreen) {
			int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				flags = flags | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			}
			getWindow().getDecorView().setSystemUiVisibility(flags);
		}
	}
	
	@Override
	protected void onPause() {
		mWebView.pause();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		if (VERBOSE) Log.v(TAG, "onDestroy, ");
		getApplicationContext().getContentResolver().unregisterContentObserver(mAudioContentObserver);
		unregisterReceiver(mConnectivityListener);
		mConnectivityListener.callback = null;
		mConnectivityListener = null;
		super.onDestroy();
	}
	
	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mInitialized = false;
		mWebView.release();
	}
	
	@Override
	public void onVideoLoaded(int duration) {
		mInitialized = true;
		mPlayerSeek.setMax(duration);
	}
	
	@Override
	public void onTimeUpdated(int time) {
		mPlayerSeek.setProgress(time);
	}
	
	private class AudioContentObserver extends ContentObserver {
		
		AudioContentObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (VERBOSE) Log.v(TAG, "onChange, ");
			updateVolume();
		}
	}
}