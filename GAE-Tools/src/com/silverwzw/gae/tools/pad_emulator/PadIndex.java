package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		setDefaultAction(new ControlPanel());
	}
	public boolean preServ(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String hash;
		hash = PadEmulatorSettings.currentUserHash();
		if ("cbf9d8da00cdc95dcd017fe07028029f".equals(hash)) {
			return true;
		}
		if (!PadEmulatorSettings.googleSet().contains(hash)) {
			resp.sendError(401,hash);
			return false;
		}
		return true;
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
		Channel.broadcast("{\"type\":\"newVersion\"}");
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
		resp.getWriter().print(PadEmulatorSettings.instance(req.getParameter("pid")).downloadData.getSavedDataJSON());
	}
}
