package com.kishanprao.youtubehelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Kishan P Rao on 11/06/17.
 */

class NetworkConnectivityListener extends BroadcastReceiver {
	public boolean isLoaded = false;
	public ConnectivityCallback callback;
	
	interface ConnectivityCallback {
		void onNetworkConnected();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (!isLoaded && isNetworkConnected(context)) {
			if (callback != null) {
				callback.onNetworkConnected();
			}
		}
	}
	
	public boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}
}
