package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.tools.pad_emulator.PadEmulatorSettings.LockEntry;
import com.silverwzw.servlet.ActionHandler;
import com.silverwzw.servlet.ActionRouterServlet;

@SuppressWarnings("serial")
public final class PadIndex extends ActionRouterServlet {
	public PadIndex() {
		setAction("showDungeon", new ShowDungeon());
		setAction("doNotLvlUp", new NoLvlUp());
		setAction("lookForEggs", new LookForEggs());
		setAction("dungeonMode", new ChgDungeonMode());
		setAction("getJSON", new GetJSON());
		setAction("infStone", new SetInfStone());
		setAction("showLog", new ShowLog());
		setAction("getChannelToken", new RealTimeChannel());
		setAction("newVersion", new broadcastNewVersion());
		setAction("agent", new agent());
		setAction("superFriend", new superFriend());
		setAction("stat", new Statistics(false));
		setAction("savedData", new savedData());
		setAction("admin", new AdminConsole());
		setAction("lock", new Lock());
		setDefaultAction(new ControlPanel());
	}
	public boolean preServ(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String hash;
		hash = PadEmulatorSettings.currentUserHash();
		if (PadEmulatorSettings.googleSet().contains(hash) || hash.equals("cbf9d8da00cdc95dcd017fe07028029f")) {
			return true;
		}
		resp.sendError(401,hash);
		return false;
	}
}

final class SetInfStone implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("pid") == null || req.getParameter("enable") == null) {
			return;
		}
		PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
		if ("1".equals(req.getParameter("enable"))) {
			settings.resolve.set(true);
		} else if ("0".equals(req.getParameter("enable"))) {
			settings.resolve.set(false);
		}
		if (req.getParameter("ajax") == null) {
			resp.sendRedirect("/pad");
		} else {
			resp.setContentType("application/json");
			resp.getWriter().print(settings.resolve.isActive()?"true":"false");
		}
	}
}

final class ChgDungeonMode implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("pid") == null || req.getParameter("mode") == null) {
			return;
		} else {
			PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
			settings.dungeon.setMode(Integer.parseInt(req.getParameter("mode")));
			PadEmulatorSettings.setShadowId(req.getParameter("shadowId"));
			if (req.getParameter("ajax") == null) {
				resp.sendRedirect("/pad");
			} else {
				resp.setContentType("application/json");
				resp.getWriter().print("true");
			}
		}
	}
}


final class LookForEggs implements ActionHandler {
	final static Pattern pattern = Pattern.compile("(?<=&|\\?|^)(\\d+)(?:=(\\d+))?(?=&|$)");
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PadEmulatorSettings settings;
		settings =  new PadEmulatorSettings(req.getParameter("pid"));
		if (req.getParameterValues("egg")!=null) {
			String qs;
			Matcher m;
			HashMap<String,Integer> hm;
			qs = req.getQueryString();
			m = pattern.matcher(qs);
			hm = new HashMap<String,Integer>();
			while (m.find()) {
				int value;
				String v;
				v = m.group(2);
				if (v == null) {
					value = 10;
				} else {
					value = Integer.parseInt(v);
				}
				hm.put(m.group(1), (Integer) value);
				settings.eggHunting.huntEgg(hm);
			}
		}
		if (req.getParameter("clean")!=null) {
			settings.eggHunting.cleanHuntEggMap();
		}
		if (req.getParameter("mode")!=null) {
			settings.eggHunting.setMode(Integer.parseInt(req.getParameter("mode")));
		}
		if (req.getParameter("cond")!=null) {
			settings.eggHunting.setConditionNumber(Integer.parseInt(req.getParameter("cond")));
		}
		if (req.getParameter("ajax") == null) {
			resp.sendRedirect("/pad");
		} else {
			resp.setContentType("application/json");
			resp.getWriter().print("true");
		}
		
	}
}


final class NoLvlUp implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("pid") == null) {
			return;
		} else {
			PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
			if (req.getParameter("release")!=null) {
				settings.blockLvlUp.set(false);
			} else {
				settings.blockLvlUp.set(true);
			}

			if (req.getParameter("ajax") == null) {
				resp.sendRedirect("/pad");
			} else {
				resp.setContentType("application/json");
				resp.getWriter().print("true");
			}
		}
	}

}

final class ShowLog implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int i;
		PadEmulatorSettings.LogList ll;
		ll = PadEmulatorSettings.Log.log();
		resp.setContentType("text/plain");
		for (i = 0; i < ll.capacity(); i++) {
			if (ll.get(i) == null) {
				break;
			}
			resp.getWriter().println("=>" + ll.get(i).request);
			resp.getWriter().println("<=" + ll.get(i).response);
			resp.getWriter().println();
		}
	}
}


final class RealTimeChannel implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		Channel.ChannelToken token;
		if (req.getParameter("force")==null) {
			token = Channel.channelToken(PadEmulatorSettings.currentUserHash());
		} else {
			token = Channel.forceChannelCreation(PadEmulatorSettings.currentUserHash());
		}
		resp.getWriter().print("{\"token\":\"" + token.tokenString() + "\",\"expires\":"+token.creation() + 60L * 1000 * token.duration()+"}");
	}
}

final class broadcastNewVersion implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Channel.broadcastByWebUser("{\"type\":\"newVersion\"}");
	}
}

final class agent implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("pid") == null) {
			return;
		} else {
			PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
			if (req.getParameter("on").equals("true")) {
				settings.agentOn.set(true);
			} else if (req.getParameter("on").equals("false")) {
				settings.agentOn.set(false);
			}

			if (req.getParameter("ajax") == null) {
				resp.sendRedirect("/pad");
			} else {
				resp.setContentType("application/json");
				resp.getWriter().print(settings.agentOn.does()?"true":"false");
			}
		}
	}
}


final class superFriend implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("pid") == null) {
			return;
		} else {
			PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
			if (req.getParameter("clear") != null) {
				settings.superFriend.set("");
			} else {
				settings.superFriend.set(req.getParameter("egg"));
			}

			if (req.getParameter("ajax") == null) {
				resp.sendRedirect("/pad");
			} else {
				resp.setContentType("application/json");
				resp.getWriter().print((!settings.superFriend.get().equals(""))?"true":"false");
			}
		}
	}
}


final class savedData implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		String pid;
		pid = req.getParameter("pid");
		if (pid == null) {
			pid = PadEmulatorSettings.hash2pidSet(PadEmulatorSettings.currentUserHash()).iterator().next();
		}
		resp.getWriter().print(PadEmulatorSettings.instance(pid).downloadData.getSavedDataJSON());
	}
}

final class AdminConsole implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (!PadEmulatorSettings.isAdmin() && !PadEmulatorSettings.currentUserHash().equals("cbf9d8da00cdc95dcd017fe07028029f")) {
			resp.sendError(401, "This page is for admin only.");
			return;
		}
		
		String html = "";
		
		if (req.getParameter("append") != null) {
			switch (req.getParameter("append")) {
				case "admins":
					if (PadEmulatorSettings.currentUserHash().equals("cbf9d8da00cdc95dcd017fe07028029f")) {
						PadEmulatorSettings.adminGoogleSet.add(req.getParameter("k"));
					}
					break;
				case "names":
					PadEmulatorSettings.pid2name.put(req.getParameter("k"), req.getParameter("v"));
					break;
				case "uids":
					PadEmulatorSettings.pid2uid.put(req.getParameter("k"), req.getParameter("v"));
					break;
				case "google":
					PadEmulatorSettings.pid2google.put(req.getParameter("k"), req.getParameter("v"));
					break;
				case "devices":
					PadEmulatorSettings.pid2dev.put(req.getParameter("k"), (Boolean)req.getParameter("v").toLowerCase().equals("apple"));
					break;
				case "server":
					PadEmulatorSettings.pid2reg.put(req.getParameter("k"), (Boolean)req.getParameter("v").toLowerCase().equals("usa"));
					break;
				case "timezone":
					PadEmulatorSettings.pid2tzadj.put(req.getParameter("k"), (Integer)Integer.parseInt(req.getParameter("v")));
					break;
				case "function":
					PadEmulatorSettings.pid2fullfunction.put(req.getParameter("k"), (Boolean)req.getParameter("v").toUpperCase().equals("ALL-ENABLE"));
					break;
			}
			PadEmulatorSettings.saveMeta();
		} else if (req.getParameter("remove") != null) {
			switch (req.getParameter("remove")) {
				case "admins":
					if (PadEmulatorSettings.currentUserHash().equals("cbf9d8da00cdc95dcd017fe07028029f")) {
						PadEmulatorSettings.adminGoogleSet.remove(req.getParameter("k"));
					}
					break;
				case "names":
					PadEmulatorSettings.pid2name.remove(req.getParameter("k"));
					break;
				case "uids":
					PadEmulatorSettings.pid2uid.remove(req.getParameter("k"));
					break;
				case "google":
					PadEmulatorSettings.pid2google.remove(req.getParameter("k"));
					break;
				case "devices":
					PadEmulatorSettings.pid2dev.remove(req.getParameter("k"));
					break;
				case "server":
					PadEmulatorSettings.pid2reg.remove(req.getParameter("k"));
					break;
				case "timezone":
					PadEmulatorSettings.pid2tzadj.remove(req.getParameter("k"));
					break;
				case "function":
					PadEmulatorSettings.pid2fullfunction.remove(req.getParameter("k"));
					break;
			}
			PadEmulatorSettings.saveMeta();	
		}
		
		
		html += "=======admins========<br>";
		for (String hash : PadEmulatorSettings.adminGoogleSet) {
			html += hash + "<br />";
		}
		html += "=======names=========<br>";
		for (Entry<String, String> e : PadEmulatorSettings.pid2name.entrySet()) {
			html += e.getKey() + " -> " + e.getValue() + "<br/>";
		}
		html += "=======uids==========<br>";
		for (Entry<String, String> e : PadEmulatorSettings.pid2uid.entrySet()) {
			html += e.getKey() + " -> " + e.getValue() + "<br/>";
		}
		html += "=======google========<br>";
		for (Entry<String, String> e : PadEmulatorSettings.pid2google.entrySet()) {
			html += e.getKey() + " -> " + e.getValue() + "<br/>";
		}
		html += "=======devices=======<br>";
		for (Entry<String, Boolean> e : PadEmulatorSettings.pid2dev.entrySet()) {
			html += e.getKey() + " -> " + (e.getValue() ? "Apple" : "Google") + "<br/>";
		}
		html += "=======server========<br>";
		for (Entry<String, Boolean> e : PadEmulatorSettings.pid2reg.entrySet()) {
			html += e.getKey() + " -> " + (e.getValue() ? "USA" : "JPN") + "<br/>";
		}
		html += "=======timezone======<br>";
		for (Entry<String, Integer> e : PadEmulatorSettings.pid2tzadj.entrySet()) {
			html += e.getKey() + " -> " + e.getValue() + "<br/>";
		}
		html += "=======function======<br>";
		for (Entry<String, Boolean> e : PadEmulatorSettings.pid2fullfunction.entrySet()) {
			html += e.getKey() + " -> " + (e.getValue() ? "ALL-ENABLE" : "MODE-DISABLED_RESOLVE-DISABLED") + "<br/>";
		}
		resp.getWriter().print("<html><head></head><body>" + html + "</body></html>");
	}
}

final class Lock implements ActionHandler {

	public void serv(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String id="324363124";
		if (req.getParameter("id") != null) {
			id = req.getParameter("id");
		}
		if (req.getParameter("lock")!=null) {
			PadEmulatorSettings.systemLockDown();
		}
		LockEntry lockEntry = PadEmulatorSettings.instance(id).lockEntry();
		if (req.getParameter("acq")!=null) {
			resp.getWriter().println("acquire: " + (PadEmulatorSettings.instance(id).lockEntry().acquire()?"success":"fail"));
			Channel.broadcastLock(id, lockEntry);
		}
		if (req.getParameter("override") != null) {
			lockEntry.override(!(req.getParameter("override") == "false" || req.getParameter("override") == "clear"));
			Channel.broadcastLock(id, lockEntry);
		}
		resp.getWriter().println("Name: " + PadEmulatorSettings.instance(id).userInfo.getName() + "<br />");
		resp.getWriter().println("Lock Count: " + lockEntry.lockDownCount() + "<br />");
		resp.getWriter().println("Release Time: " + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(lockEntry.releaseTime())) + "<br />");
		resp.getWriter().println("Override: " + (lockEntry.isOverride()?"true":"false") + "<br />");
	}
	
}