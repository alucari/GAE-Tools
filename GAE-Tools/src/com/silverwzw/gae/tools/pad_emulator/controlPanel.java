package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.ActionHandler;

final public class controlPanel extends ActionHandler {
	static String sp = "&nbsp;";
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		java.io.PrintWriter o;
		
		HashMap<String,String> users;
		users = new HashMap<String,String>();
		users.put("324363124","silverwzw");
		users.put("324151024","tea");
		
		o = resp.getWriter();
		
		o.println("<html><head><script src=\"/monster.js\"></script></head><body>");
		o.println("<table border=\"1\"><tbody>");
		o.println("<tr><th>ID</th><th>name</th><th>Enable</th><th>Last Dungeon</th><th>Level Lock</th><th>Dungeon Lock</th><th>Egg Hunting</th><th>Wanted Eggs</th></tr>");
		for (Entry<String, String> e : users.entrySet()) {
			PadEmulatorSettings settings;
			String pid,str;
			boolean disable;
			pid = e.getKey();
			settings = new PadEmulatorSettings(pid);
			o.print("<tr id=\"" + pid + "\">");
			o.print(td(pid));
			o.print(td(e.getValue()));
			disable = settings.isDungeonModDisabled();
			str = font(disable?"N":"Y","allDisable") + sp + a("/pad?action=functionEnableDisable&pid="+pid+"&enable=1","[+]") + a("/pad?action=functionEnableDisable&pid="+pid+"&disable=1","[-]");
			o.print(td(str));
			
			o.print(td(a("/pad?action=showDungeon&pid=" + pid, "show", "_blank")));
			o.print(td(font(settings.isBlockLevelUp()?"Y":"N","isBlockLevelUp")  + sp + a("/pad?action=doNotLvlUp&pid=" + pid, "[!+]") + a("/pad?action=doNotLvlUp&release=1&pid=" + pid, "[-]")));

			o.print(td(font(settings.isLocked()?"Y":"N","isLocked") + a("/pad?action=lookForEggs&release=1&pid=" + pid,"[C]")));
			o.print(td(font(settings.isLookingForCertainEgg()?"Y":"N","isLookingForCertainEgg") + sp + a("/pad?action=lookForEggs&start=1&pid="+pid,"[!+]") + a("/pad?action=lookForEggs&stop=1&pid="+pid,"[-]")));
			o.print("<td bgcolor='" + (settings.isLookingForCertainEgg()?"green":"red") + "'>");
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
			o.print(a("/pad?action=lookForEggs&clean=1&pid="+ pid,"[C]"));
			o.print("</td>");
			o.print("</tr>");
		}
		o.println("</tbody></table>");
		o.println("</body></html>");
	}
	final private static String td(String labelContent) {
		return "<td>" + labelContent + "</td>";  
	}
	final private static String a(String link, String content) {
		return a(link,content,"_top");
	}
	final private static String a(String link, String content, String target) {
		return "<a href='" + link + "' target='" + target + "'>"+content + "</a>";
	}
	final private static String font(String content, String className) {
		return "<font class='" + className + "'>" + content + "</font>";
	}
}
