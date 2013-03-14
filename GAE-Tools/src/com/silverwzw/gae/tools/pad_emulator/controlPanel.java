package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.ActionHandler;

final public class controlPanel extends ActionHandler {
	static String sp = "&nbsp;";
	public void serv(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		java.io.PrintWriter o;
		
		HashMap<String,String> users;
		users = new HashMap<String,String>();
		users.put("324363124","silverwzw");
		
		o = resp.getWriter();
		
		o.println("<html><head><script src=\"/monster.js\"></script></head><body>");
		o.println("<table border=\"1\"><tbody>");
		o.println("<tr><th>ID</th><th>name</th><th>Enable</th><th>Last Dungeon Entered</th><th>block Level Up?</th><th>Wanted Eggs</th><th>Other</th></tr>");
		for (Entry<String, String> e : users.entrySet()) {
			PadEmulatorSettings settings;
			String pid,str;
			boolean disable;
			pid = e.getKey();
			settings = new PadEmulatorSettings(pid);
			o.print("<tr>");
			o.print(td(pid));
			o.print(td(e.getValue()));
			disable = settings.isAllFunctionDisabled();
			str = (disable?"N":"Y") + sp + a("/pad?action=functionEnableDisable&pid="+pid+(disable?"&enable=1":"&disable=1"),disable?"[turn on]":"[turn off]");
			o.print(td(str));
			
			o.print(td(a("/pad?action=showDungeon&pid=" + pid, "show", "_blank")));
			if (settings.isBlockLevelUp()) {
				o.print(td("Y" + sp + a("/pad?action=doNotLvlUp&release=1&pid=" + pid, "[release]")));
			} else {
				o.print(td("N" + sp + a("/pad?action=doNotLvlUp&pid=" + pid, "[!!acquire]")));
			}
			o.print("<td>");
			if (settings.isLookingForCertainEgg()) {
				str = "";
				for (String egg : settings.WantedEggs()) {
					str += "document.write(show(" + egg + "));";
				}
				if (str.equals("")) {
					str = "Yes, but no eggs set";
				} else {
					str = "<script>" + str + "</script>"; 
				}
				o.print(str);
			} else {
				o.print("N/A");
			}
			o.print("</td>");
			o.print("<td>");
			if (settings.isLocked()) {
				o.print(a("/pad?action=lookForEggs&release=1&pid=" + pid,"[release lock]") + sp);
			}
			o.print("<a href='#' onclick='addEgg(" + pid + ");'>[add a egg]</a>" + sp);
			o.print(a("/pad?action=lookForEggs&clean=1&pid="+ pid,"[reset egg list]" + sp));
			if (settings.isLookingForCertainEgg()) {
				o.print(a("/pad?action=lookForEggs&stop=1&pid="+ pid,"[stop egg hunt]" + sp));
			} else {
				o.print(a("/pad?action=lookForEggs&start=1&pid=" + pid,"[!!start egg hunt]" + sp));
			}
			o.print("</td>");
			o.print("</tr>");
		}
		o.println("</tbody></table>");
		o.println("</body></html>");
	}
	private static String td(String labelContent) {
		return "<td>" + labelContent + "</td>";  
	}
	private static String a(String link, String content) {
		return a(link,content,"_top");
	}
	private static String a(String link, String content, String target) {
		return "<a href='" + link + "' target='" + target + "'>"+content + "</a>";
	}

}
