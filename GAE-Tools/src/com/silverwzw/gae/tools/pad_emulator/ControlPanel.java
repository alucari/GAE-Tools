package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.servlet.ActionHandler;


final public class ControlPanel implements ActionHandler {
	static String sp = "&nbsp;";
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		java.io.PrintWriter o;
		int i;
		
		o = resp.getWriter();
		
		o.println("<html><head><title>Silverwzw's Puzzle & Dragon Cracker - control panle</title><script src=\"/pad/monsterDB.js\"></script><script src=\"/pad/monster.js\"></script><script src='//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'></script><script src='/_ah/channel/jsapi'></script><script src='/pad/pad.js'></script></head><body>");
		o.println("<table border=\"1\"><tbody>");
		o.println("<tr><th>ID</th><th>name</th><th>Mode</th><th>Dungeon</th><th>Resolve</th><th>Level Lock</th><th>Egg Hunting</th><th>Wanted Eggs</th></tr>");
		for (Entry<String, String> e : PadEmulatorSettings.userMapGunghoPid.entrySet()) {
			PadEmulatorSettings settings;
			String pid,str;
			int mode;
			pid = e.getKey();
			settings = new PadEmulatorSettings(pid);
			o.print("<tr id=\"" + pid + "\" class='inforow'>");
			o.print(td(pid));
			o.print(settings.getHash().equals(Channel.hash())?td(e.getValue(),"notification"):td(e.getValue()));
			mode = settings.getDungeonMode();
			str = font("Mode "+ ((Integer) mode).toString() ,"dungeonMode") + sp + sp + ajax("/pad?action=dungeonMode&pid="+pid+"&mode=1","[1]","Baddie Mode") + ajax("/pad?action=dungeonMode&pid="+pid+"&mode=2","[2]", "Weak Mode") + ajax("/pad?action=dungeonMode&pid="+pid+"&mode=3","[3]","Mask Mode") + ajax("/pad?action=dungeonMode&pid="+pid+"&mode=0","[N]","Disable all");
			o.print(td(str));
			
			o.print(td(a("/pad/showDungeon.html?pid=" + pid, "show", "dungeon_view")));
			o.print(td(font(settings.isInfStone()?"Y":"N","isInfStone") + sp + ajax("/pad?action=infStone&enable=1&pid=" + pid,"[Y]") + ajax("/pad?action=infStone&enable=0&pid=" + pid,"[N]")));
			o.print(td(font(settings.isBlockLevelUp()?"Y":"N","isBlockLevelUp")  + sp + ajax("/pad?action=doNotLvlUp&pid=" + pid, "[!Y!]") + ajax("/pad?action=doNotLvlUp&release=1&pid=" + pid, "[N]")));

			String eggTypeS;
			int eggTypeI;
			eggTypeI = settings.isLookingForCertainEgg();
			if (eggTypeI == 1){
				eggTypeS = "Y";
			} else if (eggTypeI == 2){
				eggTypeS = "P";
			} else {
				eggTypeS = "N";
			}
			o.print(td(font(eggTypeS,"isLookingForCertainEgg") + sp + ajax("/pad?action=lookForEggs&mode=1&pid="+pid,"[!Y!]") + ajax("/pad?action=lookForEggs&mode=2&pid="+pid,"[!P!]") + ajax("/pad?action=lookForEggs&mode=0&pid="+pid,"[N]")));
			o.print("<td>");
			str = "";
			for (String egg : settings.WantedEggs()) {
				str += "document.write(show(" + egg + "));";
			}
			if (str.equals("")) {
				str = "N/A";
			} else {
				str = "<script>" + str + "</script>"; 
			}
			o.print(font(str,"eggs"));
			o.print("<a href='#' onclick='addEgg(" + pid + ");'>[+]</a>");
			o.print(ajax("/pad?action=lookForEggs&clean=1&pid="+ pid,"[C]"));
			o.print("</td>");
			o.print("</tr>");
		}
		o.println("</tbody></table>");
		o.println("<br />Quick List&nbsp;"+a("/pad?action=resetEggList","[reset]")+":<br /><script>");
		Iterable<String> freqEggs = PadEmulatorSettings.getFreqEggs();
		int counter = 0;
		for (String egg : freqEggs) {
			o.println("document.write('[" + egg + "' + show(" + egg + ") + ']&nbsp;" +((++counter % 8 == 0)?"<br />')":"')"));
		}
		o.println("</script><hr /><table><tbody><tr><th width=150>tea</th><th width=150>x</th><th width=150>silverwzw</th></tr>");
		for(i = 0; i < 10; i++) {
			o.println("<tr id='channel" + i + "'><td class='tea'></td><td class='x'></td><td class='silverwzw'></td></tr>");
		}
		o.println("</tbody></table></body></html>");
	}
	final private static String td(String labelContent) {
		return "<td>" + labelContent + "</td>";  
	}
	final private static String td(String labelContent,String id) {
		return "<td id=" + id + ">" + labelContent + "</td>";  
	}
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
