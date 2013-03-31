var debug={};
debug.log = function (obj) {
	if (this.on) {
		console.log(obj);
	}
}
debug.on = false;

var refresh = function () {
	var id;
	var url;
	try {
		id = /pid=(\d*)/.exec(/(^\S+)\?(\S*)$/.exec(location.href)[2])[1];	
	} catch (e) {
		return;
	}
	url='http://tools.silverwzw.com/pad?action=getJSON&dungeon&pid='+id;
	$.get(url, function(json,code){
		debug.log(json);
		if (json == null) {
			alert("cache expired.");
			return;
		}
		var waves = json.waves;
		var monsterD = [],box=[],egg=[];
		var i,j;
		var coin="",eggs="",wave="";
		for (i = 0; i < waves.length; i++) {
			var data = waves[i].monsters;
			var monsterW=[];
			for (j = 0; j < data.length; j++) {
				if (data[j].item > 0) {
					if (data[j].item == 900) {
						box.push(data[j].inum);
					} else {
						egg.push({'id':data[j].item,'lv':data[j].inum,'plus':(data[j].pval>0)});
					}
				}
				monsterW.push({'id':data[j].num,'type':data[j].type,'lv':data[j].lv});
			}
			monsterD.push(monsterW);
		}
		
		for(i = 0, j = 0; i < box.length; i++) {
			if(coin != "") {
				coin += '+';
			}
			coin += box[i];
			j += box[i];
		}
		coin += "=" + j;
		$('#coin')[0].innerHTML = coin;
		
		for(i = 0; i < egg.length; i++) {
			eggs += "[" + show(egg[i].id, "lv: " + egg[i].lv + (egg[i].plus?"+":"")) + (debug.on?("&nbsp;lv:" + egg[i].lv):"") + (egg[i].plus?"<font color='red'><b>PLUS</b></font>":"") + "],&nbsp;"
		}
		$('#egg')[0].innerHTML = eggs;
		
		for(i = 0; i < monsterD.length; i++) {
			wave += "wave " + i + ":<br />";
			for (j = 0; j < monsterD[i].length; j++) {
				wave += show(monsterD[i][j].id, "type: " + monsterD[i][j].type + ", lv: " + monsterD[i][j].lv);
				if (debug.on) {
					wave += ",type=" + monsterD[i][j].type + ",lv=" + monsterD[i][j].lv + "&nbsp;";
				}
			}
			wave += "<br />";
		}
		
		$('#content')[0].innerHTML = wave;
	});
}
$(document).ready(refresh);