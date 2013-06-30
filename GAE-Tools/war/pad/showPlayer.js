var debug={};
debug.log = function (obj) {
	if (this.on) {
		console.log(obj);
	}
}
debug.on = true;


var refresh = function () {
	var id;
	var url;
	try {
		id = /pid=(\d*)/.exec(/(^\S+)\?(\S*)$/.exec(location.href)[2])[1];	
	} catch (e) {
		return;
	}
	url='/pad?action=getJSON&player&pid='+id;
	$.get(url, function(json,code){
		debug.log(json);
		var deckStr = "",i, eggStr = "";
		if (json == null) {
			alert("cache expired.");
			return;
		}
		function displayByCuid(cardid) {
			var mon,i,j;
			for (i = 0; i < json.card.length; i++) {
				if (json.card[i].cuid == cardid) {
					mon = json.card[i];
					break;
				}
			}
			if (mon === undefined) {
				return show(-1);
			}
			return displayByObj(mon);
		}
		function displayByObj(mon) {
			return show(mon.no, "Lv: " + mon.lv + ", Skill: " + mon.slv + ", HP +" + mon.plus[0] + " ATK +" + mon.plus[1] + " RCV +" + mon.plus[2], false);
		};
		$('#name')[0].innerHTML = json.name;
		$('#basic')[0].innerHTML = "Level : " + json.lv + "<br />Stone : " + json.gold + "<br />Point : " + json.fripnt +" ("+ Math.floor(json.fripnt/200) +")" + "<br />Login : " + json.logins + "<br />Coins : " + json.coin + "<br />Costs : " + json.cost;
		for (i = 0; i < json.decks.length; i++) {
			var ms;
			deckStr += "Set " + i + ": ";
			ms = json.decks[i]["set_0"+i];
			for (j = 0; j < ms.length; j++) {
				deckStr += displayByCuid(ms[j]);
			}
			deckStr += "<br />";
		}
		$("#decks")[0].innerHTML = deckStr;
		for (i = 0; i < json.card.length; i++) {
			eggStr += displayByObj(json.card[i]);
			if (i%8 == 7) {
				eggStr += "<br / >"
			}
		}
		$("#eggs")[0].innerHTML = eggStr;
	});
}
$(document).ready(refresh);