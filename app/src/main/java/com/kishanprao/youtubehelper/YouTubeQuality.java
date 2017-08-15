package com.kishanprao.youtubehelper;

/**
 * Created by Kishan P Rao on 11/06/17.
 */

public enum YouTubeQuality {
	Q240("small"),
	Q360("medium"),
	Q480("large");
	private String mQuality;
	
	YouTubeQuality(String quality) {
		mQuality = quality;
	}
	
	public String getQuality() {
		return mQuality;
	}
}