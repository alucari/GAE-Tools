unknowMonster = {"n":"?","r":"?бя","a":"?","s":"?","i":"images3.wikia.nocookie.net/__cb20120612233341/pad/zh/images/thumb/f/f6/%3Fi.png/60px-%3Fi.png","c":"?","t":"?","exp":0,"hp":0,"rcv":0,"atk":0};

function getMonster(id) {
	var m;
	if (id === undefined) {
		return unknowMonster;
	}
	if (typeof id === "string") {
		id = parseInt(id);
	}
	if (id < 0) {
		return unknowMonster;
	}
	m = monster[id+""];
	if (m == undefined || m.n == "") {
		return unknowMonster;
	}
	return m;
}

function src(mid) {
	return "http://" + getMonster(mid).i;
}

function show(i, alt, description, link) {
	var t,m;
	m = getMonster(i);
	if (description === undefined) {
		description = true;
	}
	t = description ? ('[' + i + ']' +  m.r + m.n + " [" + m.s + "]" + "[" + m.a + "]" + "[" + m.t + "]") : "";
	if (alt !== undefined && alt != "") {
		t = (t == "") ? alt : (t + ", "+ alt);
	}
	if (link === undefined) {
		link = "http://zh.pad.wikia.com/wiki/" + i;
	}
	return "<a href='" + link + "' title='" + t + "' target='monster_wiki'><img src='http://" + m.i + "' width='60px' height='60px'/></a>";
}
