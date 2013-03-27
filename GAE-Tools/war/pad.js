function updateElement(b,el) {
	el.innerHTML = b?"Y":"N";
	el.parentElement.bgColor=b?"3CFF3C":"FF7373";
}

var debug={};
debug.log = function (obj) {
	if (this.on) {
		console.log(obj);
	}
}
debug.on = false;

function starter() {
	var deamon;
	var update;
	var updateUI;
	var trigger = true;
	var isUpdating = 0;
	
	var countDown;
	var ids = [];
	var trs;
	
	trs = $('tr.inforow');
	for (var i = 0; i < trs.length; i++) {
		ids[ids.length] = trs[i].id; 
	}
	countDown = 0;
	$('a#trigger').click(function (e) {
		debug.log('trigger flipped!');
		trigger=!trigger;
		isUpdating = 0;
		$('#countDown')[0].innerHTML = "";
	});
	
	updateUI = function (json, code) {
		debug.log("json data for " + json.pid + " is loaded");
		debug.log(json);
		var tr;
		
		tr = $('tr#' + json.pid);
		updateElement(json.isBlockLevelUp,tr.find('.isBlockLevelUp')[0]);
		updateElement(json.isLookingForCertainEgg,tr.find('.isLookingForCertainEgg')[0]);
		updateElement(json.safeLock,tr.find('.isLocked')[0]);
		updateElement(!json.dungeonModDisable,tr.find('.allDisable')[0]);
		
		var eggstr = "";
		for (var i = 0; i < json.wantedEggs.length; i++) {
			eggstr += show(json.wantedEggs[i]);
		}
		tr.find('.eggs')[0].innerHTML = eggstr;
		isUpdating--;
	};
	
	update = function () {
		for (var i = 0; i < ids.length; i++) {
			debug.log("retrieving json data for " + ids[i]);
			$.get('/pad?action=getJSON&pid=' + ids[i], updateUI);
			isUpdating++;
			$('#countDown')[0].innerHTML = "Update count down: Updateing.";
		}
	};
	
	deamon = function () {
		debug.log("deamon awake, countDown = " + countDown + ", trigger = " + trigger);
		if (countDown == 0) {
			if (trigger) {
				update();
			}
			countDown = 11;
		}
		if (isUpdating == 0) {
			countDown--;
			if (trigger) {
				var strs,i;
				strs = "Update count down: ";
				for (i = 0; i < countDown; i++) {
					strs += "*"; 
				}
				$('#countDown')[0].innerHTML = strs;
			}
		} else {
			$('#countDown')[0].innerHTML += '.';
		}
		setTimeout(deamon,500);
	};
	
	deamon();
}

$(document).ready(starter);