function buildimg() {
	var tds = $('td.eggid2img');
	var eggid;
	var i;
	for (i = 0; i < tds.length; i++) {
		tds[i].innerHTML=show(tds[i].innerHTML);
	}
}

function query_egg() {
	var egg;
	egg = $("input#egg")[0].value;
	if (/^(\+|[1-9]\d*)$/.exec(egg) == null) {
		alert("please input an valid egg id!\n digit or '+'");
		return;
	}
	if (egg == '+') {
		egg = "%2B";
	}
	$("div#result").load(location.href.split("?")[0] + "?action=stat&do=egg&partial&egg=" + egg, buildimg);
}

function query_dungeon() {
	var dung = $('select#dung')[0].value;
	$("div#result").load(location.href.split("?")[0] + "?action=stat&do=dung&partial&dung=" + dung, buildimg);
}

$(document).ready(buildimg);