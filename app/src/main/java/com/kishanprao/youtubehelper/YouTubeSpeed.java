package com.kishanprao.youtubehelper;

/**
 * Created by Kishan P Rao on 11/06/17.
 */

enum YouTubeSpeed {
	SUPER_SLOW(0.25f),
	VERY_SLOW(0.5f),
	SLOW(0.75f),
	NORMAL(1),
	FAST(1.25f),
	VERY_FAST(1.5f),
	SUPER_FAST(2);
	
	private final float mSpeed;
	
	YouTubeSpeed(float speed) {
		mSpeed = speed;
	}
	
	public float getSpeed() {
		return mSpeed;
	}
}
