var player;
function onYouTubeIframeAPIReady() {
	player = new YT.Player('player', {
		width: width,
		height: height,
		videoId: url,
		
		playerVars: {rel: 0, cc_load_policy:1, controls: 0, autoplay: 1, iv_load_policy: 3},
		events: {
		'onReady': onPlayerReady,
		'onStateChange': onPlayerStateChange,
		}
	});
}

function seekTo(seconds) {
	player.seekTo(seconds, true);
}

function updateSeek() {
	Android.onSeekUpdated(player.getCurrentTime());
}

function setPlaybackRate(rate) {
	player.setPlaybackRate(rate);
}