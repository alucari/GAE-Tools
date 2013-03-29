package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.ActionHandler;

final public class controlPanel extends ActionHandler {
	static String sp = "&nbsp;";
	static HashMap<String,String> users;
	static {
		users = new HashMap<String,String>();
		users.put("324363124","silverwzw");
		users.put("324151024","tea");
	}
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		java.io.PrintWriter o;
		
		o = resp.getWriter();
		
		o.println("<html><head><script src=\"/pad/monster.js\"></script><script src='//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js'></script><script src='/pad/pad.js'></script></head><body>");
		o.println("<div><a href='#' id='trigger'>[auto update on/off]</a>&nbsp;&nbsp;&nbsp;<font id='countDown'></font></div><table border=\"1\"><tbody>");
		o.println("<tr><th>ID</th><th>name</th><th>Mode</th><th>Last Dungeon</th><th>Level Lock</th><th>Dungeon Lock</th><th>Egg Hunting</th><th>Wanted Eggs</th></tr>");
		for (Entry<String, String> e : users.entrySet()) {
			PadEmulatorSettings settings;
			String pid,str;
			int mode;
			pid = e.getKey();
			settings = new PadEmulatorSettings(pid);
			o.print("<tr id=\"" + pid + "\" class='inforow'>");
			o.print(td(pid));
			o.print(td(e.getValue()));
			mode = settings.getDungeonMode();
			str = font("Mode "+ ((Integer) mode).toString() ,"dungeonMode") + sp + sp + a("/pad?action=dungeonMode&pid="+pid+"&mode=1","[1]") + a("/pad?action=dungeonMode&pid="+pid+"&mode=2","[2]" + a("/pad?action=dungeonMode&pid="+pid+"&mode=0","[-]"));
			o.print(td(str));
			
			o.print(td(a("/pad/showDungeon.html?pid=" + pid, "show", "_blank")));
			o.print(td(font(settings.isBlockLevelUp()?"Y":"N","isBlockLevelUp")  + sp + a("/pad?action=doNotLvlUp&pid=" + pid, "[!+]") + a("/pad?action=doNotLvlUp&release=1&pid=" + pid, "[-]")));

			o.print(td(font(settings.isLocked()?"Y":"N","isLocked") + a("/pad?action=lookForEggs&release=1&pid=" + pid,"[C]")));
			o.print(td(font(settings.isLookingForCertainEgg()?"Y":"N","isLookingForCertainEgg") + sp + a("/pad?action=lookForEggs&start=1&pid="+pid,"[!+]") + a("/pad?action=lookForEggs&stop=1&pid="+pid,"[-]")));
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
			o.print(a("/pad?action=lookForEggs&clean=1&pid="+ pid,"[C]"));
			o.print("</td>");
			o.print("</tr>");
		}
		o.println("</tbody></table>");
		o.println("<br />Quick List&nbsp;<a href='/pad?action=resetEggList'>[reset]</a>:<br /><script>");
		Iterable<String> freqEggs = PadEmulatorSettings.getFreqEggs();
		int counter = 0;
		for (String egg : freqEggs) {
			o.println("document.write('[" + egg + "' + show(" + egg + ") + ']&nbsp;" +((++counter % 8 == 0)?"<br />')":"')"));
		}
		String calendar = "<iframe src='https://www.google.com/calendar/embed?showTitle=0&amp;showNav=0&amp;showDate=0&amp;showPrint=0&amp;showCalendars=0&amp;showTz=0&amp;mode=AGENDA&amp;height=500&amp;wkst=1&amp;bgcolor=%23FFFFFF&amp;src=8k2qgo6ku4h30bjo0u6454fgv0%40group.calendar.google.com&amp;color=%232F6309&amp;ctz=America%2FNew_York' style=' border-width:0 ' width='800' height='500' frameborder='0' scrolling='no'></iframe>";
		o.println("</script><br /><br />" + calendar + "</body></html>");
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
