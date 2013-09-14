package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.servlet.ActionHandler;


final public class ControlPanel implements ActionHandler {
	static String sp = "&nbsp;";
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		java.io.PrintWriter o;
		int i;
		Set<String> myPid;
		boolean fullFunction = true;

		o = resp.getWriter();
		
		//===========Set up myPid and showModeOption============
		myPid = new HashSet<String>();
		for (String s : PadEmulatorSettings.pidSet()) {
			PadEmulatorSettings st;
			st = new PadEmulatorSettings(s);
			if (st.userInfo.getHash().equals(PadEmulatorSettings.currentUserHash())) {
				myPid.add(s);
				if (!st.userInfo.isFullFuntion()) {
					fullFunction = false;
				}
			}
		}
		
		resp.setHeader("Content-Type","text/html; charset=utf-8");
		
		
		//===========HTML head=================
		String script;
		o.println("<html><head>" +
				"<title>Silverwzw's Puzzle & Dragon Cracker - control panle</title>" +
				"<script type='text/javascript' src='//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'></script>" +
				"<script type='text/javascript' src='/_ah/channel/jsapi'></script>" +
				"<script type='text/javascript' src=\"/pad/monsterDB.js\"></script>" +
				"<script type='text/javascript' src=\"/pad/monster.js\"></script>" +
				"<script src='/pad/pad.js'></script>");
				script = "<script>";
				script += "window['fullFunction'] = " + (fullFunction ? "true" : "false") + ";";
				script += "window['myPid'] = [];";
				for (String s : myPid) {
					script += "window['myPid'].push('" + s + "');";
				}
				script += "window['users'] = [];";
				
				
				Collection<String> jpuserlist;
				Set<Entry<String,String>> userlist;
				jpuserlist = new HashSet<String>();
				userlist = PadEmulatorSettings.pid2nameEntrySet();
				
				for (Entry<String, String> e : userlist) {
					if (PadEmulatorSettings.isAdmin() || PadEmulatorSettings.instance(e.getKey()).userInfo.getHash().equals(PadEmulatorSettings.currentUserHash())) {
						script += "window['users'].push('" + e.getValue() + "');";
					}
					if (!PadEmulatorSettings.instance(e.getKey()).userInfo.regionIsUS()) {
						jpuserlist.add(e.getKey());
					}
				}
				script += "window['jp_ids'] = [";
				i = 0;
				for (String name : jpuserlist) {
					i++;
					script += "'" + name + "'";
					if (i != jpuserlist.size()) {
						script += ',';
					}
				}
				script += "];";
				
				script += "window['ids']=[];";
				Set<String> pidSet;
				pidSet = PadEmulatorSettings.pidSet();
				for (String pid : pidSet) {
					if (PadEmulatorSettings.isAdmin() || PadEmulatorSettings.instance(pid).userInfo.getHash().equals(PadEmulatorSettings.currentUserHash())) {
						script += "window['ids'].push('" + pid + "');";
					}
				}
				
				script += "window['tzadj']={};";
				for (Entry<String, Integer> e : PadEmulatorSettings.pid2tzadj.entrySet()) {
					script += "window['tzadj']['" + e.getKey() + "']=" + e.getValue() + ";";
				}
				
				script += "window['lock_interval'] = " + PadEmulatorSettings.LockEntry.interval()+ ";";
				script += "window['lock_ban'] = " + PadEmulatorSettings.LockEntry.ban()+ ";";
				script += "window['lock_rec'] = " + PadEmulatorSettings.LockEntry.rec()+ ";";
				script += "window['lock_max'] = " + PadEmulatorSettings.LockEntry.max()+ ";";
				script += "</script>";
		
				o.print(script + "</head><body>");
		
		//==========HTML body==================
		//==========Main table=================
		o.println("<table border=\"1\"><tbody>");
		o.println("<tr><th>name</th><th>sta</th><th>agent</th>" + (fullFunction ? "<th>Mode</th>" : "<!--no mode-->") + "<th>Dungeon</th>" + (fullFunction ? "<th>Resolve</th>":"<!--no resolve-->") + "<th>Level Lock</th><th>Super Friend</th><th>Dungeon Lock</th><th>Egg Hunting</th><th>Wanted Eggs</th></tr>");
		for (String pid : PadEmulatorSettings.pidSet()) {
			PadEmulatorSettings settings;
			String str;
			int mode;
			settings = new PadEmulatorSettings(pid);
			if (!settings.userInfo.getHash().equals(PadEmulatorSettings.currentUserHash()) && !PadEmulatorSettings.isAdmin()) {
				continue;
			}
			o.print("<tr class='inforow pid" + pid+"'>");
			o.print(td(a("/pad/showPlayer.html?pid=" + pid, settings.userInfo.getName(), "player_view"),"name notification"));
			o.print(td("","sta"));
			o.print(td(font(settings.agentOn.does()?"Y":"N","agentOn") + sp + ajax("/pad?action=agent&on=true&pid="+pid,"[+]","trun on") + ajax("/pad?action=agent&on=false&pid="+pid,"[-]","trun off")));
			mode = settings.dungeon.getMode();
			str = font("Mode "+ ((Integer) mode).toString() ,"dungeonMode") + sp + sp + ajax("/pad?action=dungeonMode&pid="+pid+"&mode=1","[1]","Baddie Mode") + ajax("/pad?action=dungeonMode&pid="+pid+"&mode=2","[2]", "Weak Mode") + ajax("/pad?action=dungeonMode&pid="+pid+"&mode=3","[3]","Mask Mode") + ajax("/pad?action=dungeonMode&pid="+pid+"&mode=0","[N]","Disable all");
			o.print(fullFunction ? td(str) : "<!--no mode td-->");
			
			o.print(td(a("/pad/showDungeon.html?pid=" + pid, "show", "dungeon_view")));
			
			str = font(settings.resolve.isActive()?"Y":"N","isInfStone") + sp + ajax("/pad?action=infStone&enable=1&pid=" + pid,"[Y]") + ajax("/pad?action=infStone&enable=0&pid=" + pid,"[N]");
			o.print(fullFunction?td(str):"<!--no resolve-->");
			o.print(td(font(settings.blockLvlUp.does()?"Y":"N","isBlockLevelUp")  + sp + ajax("/pad?action=doNotLvlUp&pid=" + pid, "[!Y!]") + ajax("/pad?action=doNotLvlUp&release=1&pid=" + pid, "[N]")));
			o.print(td(font("","superFriend") + "<a href='#' onclick='superFriend(" + pid + ");'>[+]</a>" + ajax("/pad?action=superFriend&clear=1&pid=" + pid, "[C]")));
			o.print(td(font("<script>window['lock'][\"" + pid + "\"] = {\"count\":" + settings.lockEntry().lockDownCount() + ",\"time\":"+settings.lockEntry().releaseTime()+"};</script>","dungeonLock")));
			String eggTypeS;
			int eggTypeI;
			eggTypeI = settings.eggHunting.getMode();
			if (eggTypeI == 1){
				eggTypeS = "Y";
			} else if (eggTypeI == 2){
				eggTypeS = "P";
			} else {
				eggTypeS = "N";
			}
			if (!eggTypeS.equals("N")) {
				eggTypeS += settings.eggHunting.getConditionNumber();
			}
			o.print(td(font(eggTypeS,"isLookingForCertainEgg") + sp + ajax("/pad?action=lookForEggs&mode=1&pid="+pid,"[!Y!]") + ajax("/pad?action=lookForEggs&mode=2&pid="+pid,"[!P!]") + "<a href='#' onclick='setConditionNumber(" + pid + ");'>[#]</a>"));
			str = "";
			for (Entry<String, Integer> e : settings.eggHunting.huntEggMap().entrySet()) {
				str += "document.write('[' + show(" + e.getKey() + ") + '=' + " + e.getValue() +" + ']');";
			}
			if (str.equals("")) {
				str = "N/A";
			} else {
				str = "<script>" + str + "</script>"; 
			}
			str = font(str,"eggs");
			str += "<a href='#' onclick='addEgg(" + pid + ");'>[+]</a>";
			str += ajax("/pad?action=lookForEggs&clean=1&pid="+ pid,"[C]");
			o.print(td(str));
			o.print("</tr>");
		}
		o.println("</tbody></table><br />");
		
		//===========Statistics================
		o.print("<table><tbody><tr><td><h3>Statistics:</h3></td><td>&emsp;</td>");
		o.print("<td><form action='/pad' method='GET' target='stat'><input type='hidden' name='action' value='stat' /><input type='hidden' name='do' value='dung' />Query Dungeon:");
		o.print(Statistics.getDungeonList(-1, "<select id='dung' name='dung'>"));
		o.print("<input type='submit' value='Submit'/></form></td>");
		o.print("<script>function check_eggID() {if (/^[1-9]\\d{0,2}$/.exec($('input#egg')[0].value.trim()) != null) {return true;} else {alert('Invalid Egg ID!');return false;}};</script>");
		o.print("<td>&emsp;</td><td><form action='/pad' onsubmit='return check_eggID();' method='GET'  target='stat'><input type='hidden' name='action' value='stat' /><input type='hidden' name='do' value='egg'/>Query Egg: <input type='text' name='egg' id='egg' size=5/><input type='submit' value='Submit'/></form></td></tr></tbody></table>");
		
		//===========Monster Lookup============
		o.print(Statistics.mlookup());
		
		//============Timeline=================
		o.println("<h3>Events:</h3>");
		o.println("<a href='#' onclick='trigger_fullversion();' id='a_fullversion'>All Events</a> ");
		o.println("<a href='#' onclick='trigger_alluser();' id='a_alluser'>All User</a> ");
		o.println("<div id='tl'></div>");
		
		//===========Action Log=================
		if (PadEmulatorSettings.isAdmin()) {
			o.print("<hr /><table><tbody><tr>");
			for (String name : PadEmulatorSettings.nameCollection()) {
				o.print("<th width=150>" +name + "</th>");
			}
			o.print("</tr>");
			for(i = 0; i < 10; i++) {
				o.print("<tr id='channel" + i + "'>");
				for (String name : PadEmulatorSettings.nameCollection()) {
					o.print("<td class='" + name + "'></td>");
				}
				o.println("</tr>");
			}
			o.println("</tbody></table>");
		}
		
		//===========HTML end===================
		o.print("</body></html>");
	}
	final private static String td(String labelContent) {
		return td(labelContent, null);  
	}
	final private static String td(String labelContent,String classList) {
		return "<td" + (classList == null ? "":" class='" + classList + '\'') + " style='white-space:nowrap'>" + labelContent + "</td>";  
	}
	@SuppressWarnings("unused")
	final private static String a(String link, String content) {
		return a(link,content,"_top");
	}
	final private static String a(String link, String content, String target) {
		return "<a href='" + link + "' target='" + target + "'>"+content + "</a>";
	}
	final private static String ajax(String link, String content) {
		return ajax(link,content,null);
	}
	final private static String ajax(String link, String content, String title) {
		if (link.indexOf('?') == -1) {
			link += "?ajax";
		} else {
			link += "&ajax";
		}
		if (title == null) {
			title = "";
		} else {
			title = " title='" + title + "' ";
		}
		return "<a href=\"#\" onclick=\"ajaxAction('"+link+"');\" " + title + ">"+content+"</a>";
	}
	final private static String font(String content, String className) {
		return "<font class='" + className + "'>" + content + "</font>";
	}
}
