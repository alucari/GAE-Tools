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
debug.autoRelease = false;

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
knownAction.push("get_user_mails");
knownAction.push("confirm_level_up");

function starter() {
	var deamon;
	var update;
	var updateUI;
	
	var ids = [];
	var trs;
	
	trs = $('tr.inforow');
	for (var i = 0; i < trs.length; i++) {
		ids[ids.length] = trs[i].id; 
	}
	
	updateUI = function (json, code) {
		debug.log("json data for " + json.pid + " is loaded");
		debug.log(json);
		var tr;

		tr = $('tr#' + json.pid);;
		updateElement(json.isBlockLevelUp,tr.find('.isBlockLevelUp')[0]);
		updateElement(json.isLookingForCertainEgg,tr.find('.isLookingForCertainEgg')[0]);
		updateElement(json.safeLock,tr.find('.isLocked')[0]);
		updateElement(json.infStone,tr.find('.isInfStone')[0]);
		if ((""+json.pid) == "324363124" && debug.autoRelease && json.safeLock) {
			$.get("/pad?action=lookForEggs&release=1&pid=324363124&ajax",function(){;});
		}
		var modeElement = tr.find('.dungeonMode')[0];
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
	
	update = function (id) {
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
	
	deamon = function () {
		debug.log("deamon awake");
		update();
		setTimeout(deamon,300000);
	};
	
	channel = function () {
		l={};
		updateChannel = function() {
			var users=["silverwzw","x","tea"];
			var i,j;
			for (i = 0; i < users.length; i++) {
				if (l[users[i]] === undefined) {
					l[users[i]] = [];
				}
			}
			for (i = 0; i < users.length; i++) {
				for (j = 0; j < l[users[i]].length && j < 10; j++) {
					var a,html;
					a = l[users[i]][l[users[i]].length-j-1];
					if (knownAction.indexOf(a)>=0) {
						html = a;
					} else {
						html = "<font color='red'>" + a + "</font>";
					}
					$("tr#channel"+j).find("td."+users[i])[0].innerHTML = html;
				}
			}
		};
		$.get('/pad?action=getChannelToken',function(json,code){
			var socket = (new goog.appengine.Channel(json.token)).open();
			socket.onopen = function(){
				debug.log("channel open");
			};
			socket.onmessage = function(msgObj){
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
					if ( l[data.user] === undefined) {
						l[data.user] = [];
					}
					if ( l[data.user].length > 10) {
						l[data.user].shift();
					}
					l[data.user].push(data.action);
					updateChannel();
				}
			};
			socket.onerror =  function(){
				debug.log("channel error");
			};
			socket.onclose = function(){
				debug.log("channel close");
			};
		});
	};
	
	deamon();
	channel();
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