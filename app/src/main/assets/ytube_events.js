var enabledCC = false;
var sentDuration = false;
function onPlayerReady(event) {
	player.setPlaybackRate(speed);
}

function onPlayerStateChange(event) {
	if (event.data == YT.PlayerState.PLAYING) {
		event.target.setPlaybackQuality(quality);
	}
	if (!sentDuration) {
		Android.setDuration(player.getDuration());
		sentDuration = true;
	}
	if (!enabledCC) {
		var module;
		var moduleCC = "cc";
		var moduleCaptions = "captions";
		if (player.getOptions().indexOf(moduleCC) !== -1) {
			module = moduleCC;
		} else if (player.getOptions().indexOf(moduleCaptions) != -1) {
			module = moduleCaptions;
		}
		
		if (player.getOptions().indexOf(module) !== -1) {
			var index = player.getOptions().indexOf(module);
//			console.log("Captions:", player.getOptions()[index]);
			player.setOption(module, "track", {"languageCode": "en"});
			console.log("closed captions are on");
			enabledCC = true;
		} else {
			console.log("closed captions are off");
		}
	}
}