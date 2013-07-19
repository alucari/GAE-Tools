function updateElement(b,el) {
	el.innerHTML = b?"Y":"N";
	el.parentElement.bgColor=b?"3CFF3C":"FF7373";
}

function padtime2timestamp(padtime, pid) {
	var offset,j,t;
	if (jp_ids.indexOf(pid) != -1) { //is JP region
		offset = "GMT+0900";
	} else {
		t = (new Date()).getTimezoneOffset();
		j = Math.floor(t/60) *100 + t % 60 + tzadj[pid];
		if (j >= 0) {
			j = '-' + ((j<1000) ? ("0" + j) : ("" + j));
		} else {
			j = -j;
			j = "+" + ((j < 1000) ? ("0" + j) : ("" + j));
		}
		offset = "GMT" + j;
	}
	
	return Date.parse("20" + padtime.substring(0,2) + " " + padtime.substring(2,4) + " "  + padtime.substring(4,6) + " " + padtime.substring(6,8) + ":" + padtime.substring(8,10) + ":"  + padtime.substring(10,12) + " " + offset);
}

var debug={};
debug.log = function (obj) {
	if (this.on) {
		console.log(obj);
	}
}
debug.on = false;

var refresh, trigger_fullversion, trigger_alluser;

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
knownAction.push("set_auth_data");
knownAction.push("quit_friend");
knownAction.push("change_name");
knownAction.push("get_product_list");
knownAction.push("adr_purchase_gold");
knownAction.push("send_user_mail");
knownAction.push("expand_num_cards");
knownAction.push("buy_friend_max");
knownAction.push("download_card_data");
knownAction.push("download_skill_data");
knownAction.push("download_enemy_skill_data");
knownAction.push("download_dungeon_data");

var bonus = {};

function starter() {
	var i,deamon;
	var sta = {};
	var fullversion = false, alluser = false;

	
	for (i = 0; i < ids.length; i++) {
		sta[ids[i]] = {"max":0,"time":""};
	}

	trigger_fullversion = function () {
		fullversion = !fullversion;
		if (fullversion) {
			$('#a_fullversion')[0].innerHTML = "Important Events Only";
		} else {
			$('#a_fullversion')[0].innerHTML = "Show All Events";
		}
	};
	
	trigger_alluser = function (){
		var i;
		alluser = !alluser;
		bonus = {};
		if (alluser) {
			for (i = 0; i < ids.length; i++) {
				updateBonus(ids[i]);
			}
			$('#a_alluser')[0].innerHTML = "Hide Other User";
		} else {
			for (i = 0; i < myPid.length; i++) {
				updateBonus(myPid[i]);
			}
			$('#a_alluser')[0].innerHTML = "List All User";
		}
	};
	
	function updateBonusTimeline () {
		var str, i, bitem, timeleft1, timeleft2, timeStr1, timeStr2;
		var func = function (i) {i = Math.floor(i); if (i<10) {return "0"+i} else {return "" + i}};
		str = "";
		for (pid in bonus) {
			str += "<h4>" + $(".pid" + pid + ">.name>a")[0].innerHTML + ":</h4><table>";
			for (i = 0; i < bonus[pid].length; i++) {
				bitem = bonus[pid][i];
				switch (bitem.type) {
					case 2:
					case 3:
					case 5:
						if (!fullversion) {
							break;
						}
					default:
						str += "<tr>";
						timeleft1 = (bitem.start - Date.parse(new Date())) / 1000;
						timeleft2 = (bitem.end - Date.parse(new Date())) / 1000;
						if (timeleft2 > 14*24*3600 || timeleft2 <= 0 ) {
							break;
						}
						if (timeleft1 == 0 && myPid.indexOf(pid) > -1) {
							window.webkitNotifications.createNotification('http://images3.wikia.nocookie.net/__cb20130410191023/pad/zh/images/thumb/6/6d/Egg5.png/60px-Egg5.png','Event Started', bitem.content).show();
						}
						if (timeleft1 > 0) {
							timeStr1 = (timeleft1 < 3600 * 100 ? "0" : "") + func(timeleft1/3600) + ":" + func((timeleft1/60)%60) + ":" + func(timeleft1%60);
						} else {
							timeStr1 = "&nbsp;&nbsp;&nbsp;<font color=red><b>Active</b></font>";
						}
						timeStr2 = (timeleft2 < 3600 * 100 ? "0" : "") + func(timeleft2/3600) + ":" + func((timeleft2/60)%60) + ":" + func(timeleft2%60);
						
						str += "<td>[" + timeStr1 + '</td><td>&nbsp;-&nbsp;</td><td>' + timeStr2 + "]</td><td> " + bitem.content + "</td>";
						
						
						str += "</tr>";
				}
			}
			str += "</table>";
		}
		$("#tl")[0].innerHTML = str;
	};
	
	function updateBonus (pid) {
		$.get('/pad?action=getJSON&pid=' + pid + '&bonus', function (json, code) {
			var i,entity,s,e;
			if (json == null || json.res != 0) {
				return;
			}
			var b, dungeonName;
			
			bonus[pid] = [];
			
			for (i = 0; i < json.bonuses.length; i++) {
				entity = json.bonuses[i];
				
				b = {"start" : padtime2timestamp(entity.s, pid), "end" : padtime2timestamp(entity.e, pid)};
				if (new Date(b.end) < new Date()) {
					continue;
				}
				
				if (entity.d === undefined) {
					dungeonName = "";
				} else if (dungeon2name[entity.d + ""] !== undefined) {
					dungeonName = dungeon2name[entity.d + ""];
				} else {
					dungeonName = "Dungeon" + entity.d;
				}
				
				switch (entity.b) {
					case 2:
						b.content = dungeonName + ": Coin x" + (entity.a/10000);
						break;
					case 3:
						b.content = dungeonName + ": Drop x" + (entity.a/10000);
						break;
					case 5:
						b.content = dungeonName + ": Stamina x" + (entity.a/10000);
						break;
					case 6:
						b.content = dungeonName + ": Show Up";
						break;
					case 9:
						b.content = "<b>God Fest!</b>";
						break;
					case 17:
						b.content = "Skill Up x" + (entity.a/10000);
						break;
					default:
						s = entity.s;
						e = entity.e;
						delete entity.s;
						delete entity.e;
						b.content = "Unknown Event " + JSON.stringify(entity);
						entity.s = s;
						entity.e = e;
						break;
				}
				b.type = entity.b;
				bonus[pid].push(b);
			}
			updateBonusTimeline();
		});
	};
	
	function updateUI(json, code) {
		console.log("updateUI get called");
		debug.log("json data for " + json.pid + " is loaded");
		debug.log(json);
		var tr;

		tr = $('tr.pid' + json.pid);
		updateElement(json.isBlockLevelUp,tr.find('.isBlockLevelUp')[0]);
		if (fullFunction) {
			updateElement(json.infStone,tr.find('.isInfStone')[0]);
		}
		updateElement(json.agentOn,tr.find('.agentOn')[0]);
		var modeElement = tr.find('.isLookingForCertainEgg')[0];
		switch (json.isLookingForCertainEgg) {
			case 0:
				modeElement.parentElement.bgColor = "FF7373";
				modeElement.innerHTML = "N";
				break;
			case 1:
				modeElement.parentElement.bgColor = "3CFF3C";
				modeElement.innerHTML = "Y" + json.conditionNumber;
				break;
			case 2:
				modeElement.parentElement.bgColor = "F1F11A";
				modeElement.innerHTML = "P" + json.conditionNumber;
				break;
		}
		if (fullFunction) {
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
		}
		sta[json.pid]={"max":json.sta_max,"time":json.sta_time};
		var eggstr = "";
		for (var i = 0; i < json.wantedEggs.length; i++) {
			eggstr += "[" + show(json.wantedEggs[i].id) + "=" + json.wantedEggs[i].v + "]";
		}
		tr.find('.eggs')[0].innerHTML = eggstr;
		tr.find('.superFriend')[0].innerHTML = (json.superFriend == "")?"":show(json.superFriend);
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
	
	var actionLog={};
	function updateChannel() {
		//var users=["silverwzw","x","tea","Tester","tea-JPN"];
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
				if (knownAction.indexOf(a.action)>=0) {
					html = a.action + (a.parameter != null ? "(" + a.parameter + ")" : "");
				} else {
					html = "<font color='red'>" + a.action + (a.parameter != null ? "(" + a.parameter + ")" : "") + "</font>";
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
			egg_str += '[' + eggid_3digits + '] ' + m.r + ' ' + m.n + ' - [' + m.a + "][" + m.t + "]\n";
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
						actionLog[data.user].push({"action":data.action,"parameter":data.parameter});
						updateChannel();
					} else if (data.type == "dungeon") {
						notify(data.dungeon.waves,data.pid);
					} else if (data.type == "newVersion") {
						if (confirm("a new version has just been deployed by Silverwzw.\n\nClick OK to refresh\nClick Cancel to stay on current version this time.")) {
							window.location.reload();
						}
					} else if (data.type == "bonus") {
						if (data.pid !== undefined && (alluser || myPid.indexOf(data.pid) > -1)) {
							updateBonus(data.pid);
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
	
	function updateStamina() {
		var i,j;
		var currentTime;
		var offset;
		var id;
		var time2full;
		var current_sta;
		var time_left;
		var func;
		
		func = function (i) {i = Math.floor(i); if (i<10) {return "0"+i} else {return "" + i}};
		//jp_ids = ["188433641"]; 
		
		for (i = 0; i < ids.length; i++) {
			id = ids[i];

			if (sta[id] === undefined || sta[id].max == 0 || sta[id].time == "") {
				$('tr.pid' + id).find(".sta")[0].innerHTML="??/?? (??:??)";
				continue;
			}
			time2full = padtime2timestamp(sta[id].time, id) - (new Date()).valueOf();
			if (time2full <= 0) {
				$('tr.pid' + id).find(".sta")[0].innerHTML = "<font color=red>" + sta[id].max + "/" + sta[id].max + " (00:00)</font>";
				continue;
			}
			current_sta = sta[id].max - Math.ceil(time2full/600000);
			time_left = (time2full%600000)/1000;
			
			$('tr.pid' + id).find(".sta")[0].innerHTML = current_sta + "/" + sta[id].max + " (" + func(time_left/60) + ":" + func(time_left%60) + ")";
			if (myPid.indexOf(id + "") > -1 && Math.floor(time2full/1000) == 600) {
				window.webkitNotifications.createNotification('http://images3.wikia.nocookie.net/__cb20130410191023/pad/zh/images/thumb/6/6d/Egg5.png/60px-Egg5.png','Stamina Overflow', id + 'Stamina will overflow in 10 minutes!').show();
			} 
		}
	}

	for (i = 0; i < myPid.length; i++) {
		bonus[myPid[i]] = [];
		updateBonus(myPid[i]);
	}
	update();
	updateBonusTimeline();
	updateStamina();
	setInterval(update,60000);
	setInterval(function () {updateStamina();updateBonusTimeline();}, 990);
	channel(false);  //try reusing an old channel resource.
	
	if (window.webkitNotifications.checkPermission() != 0) {
		$(".pid" + myPid[0]).find(".notification")[0].innerHTML += "<input type='submit' value='Notification' onclick='webkitNotifications.requestPermission();' />";
	}
}

function ajaxAction(link) {
	$.get(link);
}

function addEgg(pid) {
	var eggid,idvs,i,eggstr;
	eggid = prompt("ID of the egg and corresponding value:","178");
	if (/^([1-9]\d{0,2}(=\d+)?,)*[1-9]\d{0,2}(=\d+)?$/.exec(eggid) == null) {
		return;
	}
	idvs = eggid.split(','); //eggid.match(/[0-9]\d{0,2}/g);
	eggstr="";
	for (i = 0; i < idvs.length; i++) {
		var evp;
		evp = /^(\d+)(?:=(\d+))?$/.exec(idvs[i]);
		if (evp[2] === undefined) {
			eggstr += "&" + evp[1];
		} else {
			eggstr += "&" + evp[1] + "=" + evp[2];
		}
	}
	ajaxAction("/pad?action=lookForEggs&ajax&egg&pid=" + pid + eggstr);
}

function superFriend(pid) {
	var eggid,i,eggstr;
	eggid = prompt("Set the egg ID of your super friend");
	if (/^[1-9]\d{0,2}$/.exec(eggid) == null) {
		return;
	}
	ajaxAction("/pad?action=superFriend&ajax&egg=" + eggid + "&pid=" + pid);
}

function setConditionNumber(pid) {
	var cond;
	cond = prompt("Set the condition number");
	if (/^(0|[1-9]\d*)$/.exec(cond) == null) {
		return;
	}
	ajaxAction("/pad?action=lookForEggs&ajax&cond=" + cond + "&pid=" + pid);
}

$(document).ready(starter);