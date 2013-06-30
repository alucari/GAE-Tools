var MlookupObj = {
		display : function (i) {
			$("#mlookup_img")[0].innerHTML = show(i);

			var str = "", m = monster[i];

			//n:name a:attr s:series t:type, r:rare, c:cost, hp, atk, rcv, exp
			function d(s) {
				if (s === null || s === undefined) {
					return "?";
				} else {
					return s;
				}
			}
			str += "[" + i + "] " + d(m.r) + " "+ d(m.n) + "<br />";
			str += "[Series]" + d(m.s) + " [Attr]"+ d(m.a) + " [Type]"+ d(m.t) + " [EXP]" + d(m.exp) + "W<br />";
			str += "[Cost]" + d(m.c) + " [HP]" + d(m.hp) + " [ATK]"+ d(m.atk) + " [RCV]" + d(m.rcv) + " [Weight]" + (m.hp/10 + m.atk/5 + m.rcv/3).toFixed(1);
			
			$("#mlookup_detail")[0].innerHTML = str;
			
		},
		displayList : function (list) {
			$("#mlookup_detail")[0].innerHTML = "";
			
			var i, str;
			
			str = "";
			
			for (i = 0; i < list.length; i++) {
				str += show(list[i], "", true, "javascript:MlookupObj.display(" + list[i] + ");");
			}
			
			$("#mlookup_img")[0].innerHTML = str;
		},
		exec : function () {
			
			var keywords, i, list, b, id;
			
			$("#mlookup_img")[0].innerHTML = "";
			$("#mlookup_detail")[0].innerHTML = "";
			
			keywords = $("input#mlookup")[0].value.trim();
			list = [];

			if (/^[1-9]\d{0,3}$/.exec(keywords) != null) {
				this.display(parseInt(keywords));
				return;
			}
			
			keywords = keywords.split(" ");
			
			checkKeyword = function (m, k) {
				//n:name a:attr s:series t:type
				try {
					return (m.n.indexOf(k) >= 0) || (m.a.indexOf(k) >= 0) || (m.s.indexOf(k) >= 0) || (m.t.indexOf(k) >= 0);
				} catch (e){
					console.log(m);
					return false;
				}
			};
			
			for (id in monster) {
				b = true;
				for (i = 0; i < keywords.length; i++) {
					if (!checkKeyword(monster[id], keywords[i])) {
						b = false;
						break;
					}
				}
				if (b) {	
					list.push(id);
					if (list.length > 7) {
						break;
					}
				}
			}
			if (list.length == 1) {
				this.display(list[0]);
			} else {
				this.displayList(list);
			}
		}
};