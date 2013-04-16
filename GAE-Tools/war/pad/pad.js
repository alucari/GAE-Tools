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

var refresh;

var knownAction = [];
knownAction.push("buy_stamina");
knownAction.push("do_continue");
knownAction.push("do_continue_ack");
knownAction.push("sneak_dungeon");
knownAction.push("sneak_dungeon_ack");
knownAction.push("clear_dungeon");
knownAction.push("get_player_data");
knownAction.push("get_recommended_helpers");
knownAction.push("sell_user_cards");
knownAction.push("composite_user_cards");
knownAction.push("save_decks");
knownAction.push("login");
knownAction.push("evolve_user_card");
knownAction.push("play_gacha_cnt");
knownAction.push("play_gacha");
knownAction.push("request_friend");
knownAction.push("get_user_mails");
knownAction.push("confirm_level_up");
knownAction.push("download_limited_bonus_data");
knownAction.push("get_helpers");
knownAction.push("get_ids_parameter");
knownAction.push("get_id_parameter");
knownAction.push("accept_friend_request");
knownAction.push("get_user_mail");
knownAction.push("delete_user_mail");

function starter() {
	var deamon;
	
	var ids = [];
	var trs;
	
	trs = $('tr.inforow');
	for (var i = 0; i < trs.length; i++) {
		ids[ids.length] = trs[i].id; 
	}
	
	function updateUI(json, code) {
		debug.log("json data for " + json.pid + " is loaded");
		debug.log(json);
		var tr;

		tr = $('tr#' + json.pid);;
		updateElement(json.isBlockLevelUp,tr.find('.isBlockLevelUp')[0]);
		updateElement(json.infStone,tr.find('.isInfStone')[0]);
		updateElement(json.agentOn,tr.find('.agentOn')[0]);
		var modeElement = tr.find('.isLookingForCertainEgg')[0];
		switch (json.isLookingForCertainEgg) {
			case 0:
				modeElement.parentElement.bgColor = "FF7373";
				modeElement.innerHTML = "N";
				break;
			case 1:
				modeElement.parentElement.bgColor = "3CFF3C";
				modeElement.innerHTML = "Y";
				break;
			case 2:
				modeElement.parentElement.bgColor = "F1F11A";
				modeElement.innerHTML = "P";
				break;
		}
		modeElement = tr.find('.dungeonMode')[0];
		switch (json.dungeonMode){
			case 1:
				modeElement.parentElement.bgColor = "3CFF3C";
				modeElement.innerHTML = "Mode " + json.dungeonMode;
				break;
			case 2:
				modeElement.parentElement.bgColor = "73FFFF";
				modeElement.innerHTML = "Mode " + json.dungeonMode;
				break;
			case 3:
				modeElement.parentElement.bgColor = "FF3CFF";
				modeElement.innerHTML = "Mode " + json.dungeonMode;
				break;
			default:
				modeElement.parentElement.bgColor = "FF7373";
				modeElement.innerHTML = "Disabled";
				break;
		}
		var eggstr = "";
		for (var i = 0; i < json.wantedEggs.length; i++) {
			eggstr += show(json.wantedEggs[i]);
		}
		tr.find('.eggs')[0].innerHTML = eggstr;
	};
	
	function update(id) {
		if (id === undefined) { // update all
			for (var i = 0; i < ids.length; i++) {
				debug.log("retrieving json data for " + ids[i]);
				$.get('/pad?action=getJSON&pid=' + ids[i], updateUI);
			}
		} else {
			debug.log("retrieving json data for " + id);
			$.get('/pad?action=getJSON&pid=' + id, updateUI);
		}
	};
	
	refresh = update;
	
	function deamon () {
		debug.log("deamon awake");
		update();
		setTimeout(deamon,300000);
	};
	
	var actionLog={};
	function updateChannel() {
		var users=["silverwzw","x","tea"];
		var i,j;
		for (i = 0; i < users.length; i++) {
			if (actionLog[users[i]] === undefined) {
				actionLog[users[i]] = [];
			}
		}
		for (i = 0; i < users.length; i++) {
			for (j = 0; j < actionLog[users[i]].length && j < 10; j++) {
				var a,html;
				a = actionLog[users[i]][actionLog[users[i]].length-j-1];
				if (knownAction.indexOf(a)>=0) {
					html = a;
				} else {
					html = "<font color='red'>" + a + "</font>";
				}
				$("tr#channel"+j).find("td."+users[i])[0].innerHTML = html;
			}
		}
	};
	
	function notify(dungeonW,pid) {
		debug.log(dungeonW);
		if (dungeonW === undefined) {
			return;
		}
		if (!window.webkitNotifications) {
			return; //not support
		}
		if (window.webkitNotifications.checkPermission() != 0) {
			return; // no permission
		}
		var dungeonIcon = "";
		var dungeonEggs = [];
		var i,j;
		var dungeonM,m,egg_str="",eggid_3digits;
		var lockIcon = false;
		for (i = 0; i < dungeonW.length; i++) {
			dungeonM = dungeonW[i].monsters; 
			for (j = 0; j < dungeonM.length; j++) {
				m = dungeonM[j];
				if (m.type > 0 && !lockIcon) {
					if (m.type > 1) {
						lockIcon = true;
					}
					dungeonIcon = src(m.num);	
				}
				if (m.item != "0" && m.item != "900") {
					dungeonEggs.push(parseInt(m.item));
				}
			}
		}
		if (dungeonIcon == "") {
			dungeonIcon = "http://" + unknowMonster.i;
		}
		for (i = 0; i < dungeonEggs.length; i++) {
			m = getMonster(dungeonEggs[i]);
			
			if (dungeonEggs[i] < 10) {
				eggid_3digits = "00";
			} else if (dungeonEggs[i] < 100) {
				eggid_3digits = "0";
			} else {
				eggid_3digits = "";
			}
			eggid_3digits += dungeonEggs[i];
			egg_str += '[' + eggid_3digits + '] ' + m.r + ' ' + m.n + ' - (' + m.a + ')' + m.s + '\n';
		}
		var ntfy;
		ntfy = window.webkitNotifications.createNotification(dungeonIcon,'Reward',egg_str);
		ntfy.onclick = function () {
			window.open('/pad/showDungeon.html?pid='+pid,'dungeon_view');
		};
		ntfy.show();
		setTimeout(function(){
			ntfy.close();
		},30000);
	};
	
	function channel(force) {
		if (force === undefined || force != true) {
			force = false;
		}
		$.get('/pad?action=getChannelToken' + (force?'&force':''), function(json,code){
			(new goog.appengine.Channel(json.token)).open({
				"onopen" : function(){
					debug.log("channel open");
				},
				"onmessage" : function(msgObj){
					var data;
					debug.log(msgObj);
					data = eval('('+msgObj.data+')');
					debug.log(data);
					if (data.type == "refresh") {
						if (data.pid === undefined) {
							update();
						} else {
							update(data.pid);
						}
					} else if (data.type == "newAction"){
						if ( actionLog[data.user] === undefined) {
							actionLog[data.user] = [];
						}
						if ( actionLog[data.user].length > 10) {
							actionLog[data.user].shift();
						}
						actionLog[data.user].push(data.action);
						updateChannel();
					} else if (data.type == "dungeon") {
						notify(data.dungeon.waves,data.pid);
					} else if (data.type == "newVersion") {
						if (confirm("a new version has just been deployed by Silverwzw.\n\nClick OK to refresh\nClick Cancel to stay on current version this time.")) {
							window.location.reload();
						}
					}
				},
				"onerror" : function(e){
					console.log("channel error");
					console.log(e);
					if(/Token(?:\s|\+)timed(?:\s|\+)out/i.exec(e.description) != null) {
						channel(true);
					}
				},
				"onclose" : function(){
					console.log("channel close");
				}
			});
		});
	};
	
	deamon();
	channel(false);  //try reusing an old channel resource.
	
	if (window.webkitNotifications.checkPermission() != 0) {
		$("#notification")[0].innerHTML += "<input type='submit' value='Notification' onclick='webkitNotifications.requestPermission();' />";
	}
}

function ajaxAction(link) {
	$.get(link);
}

function addEgg(pid) {
	var eggid,ids,i,eggstr;
	eggid = prompt("ID of the egg:");
	if (/^(?:[1-9]\d{0,2},)*[1-9]\d{0,2}$/.exec(eggid) == null) { //441,108,44,45,254,189
		return;
	}
	ids = eggid.match(/[0-9]\d{0,2}/g);
	eggstr="";
	for (i = 0; i < ids.length; i++) {
		eggstr += "&egg=" + ids[i];
	}
	ajaxAction("/pad?action=lookForEggs&ajax&pid=" + pid + eggstr);
}

$(document).ready(starter);