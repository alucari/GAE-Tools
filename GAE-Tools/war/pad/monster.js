unknowMonster = {"n":"?","r":"бя?","a":"?","s":"?","i":"images3.wikia.nocookie.net/__cb20120612233341/pad/zh/images/thumb/f/f6/%3Fi.png/60px-%3Fi.png"};

function getMonster(id) {
	var m;
	if (id === undefined) {
		return unknowMonster;
	}
	if (typeof id === "string") {
		id = parseInt(id);
	}
	m = monster[id];
	if (m.n == "") {
		return unknowMonster;
	}
	return m;
}

function src(mid) {
	return "http://" + getMonster(mid).i;
}

function show(i, alt) {
	var t,m;
	m = getMonster(i);
	t = '[' + i + ']' +  m.r + m.n;
	if (alt !== undefined) {
		t = t + ", "+ alt;
	}
	return "<a href='http://zh.pad.wikia.com/wiki/" + i + "' title='" + t + "' target='monster_wiki'><img src='http://" + m.i + "' width='60px' height='60px'/></a>";
}
