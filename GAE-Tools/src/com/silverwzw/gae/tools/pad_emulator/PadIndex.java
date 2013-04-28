package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;

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
		setAction("resetEggList", new ResetEggList());
		setAction("infStone", new SetInfStone());
		setAction("showLog", new ShowLog());
		setAction("getChannelToken", new RealTimeChannel());
		setAction("newVersion", new broadcastNewVersion());
		setAction("agent", new agent());
		setDefaultAction(new ControlPanel());
	}
	public boolean preServ(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String hash;
		hash = PadEmulatorSettings.currentUserHash();
		if (!PadEmulatorSettings.googleSet().contains(hash)) {
			resp.sendError(401,hash);
			return false;
		}
		return true;
	}
}

final class ResetEggList implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PadEmulatorSettings.resetFreqEgg();
		resp.sendRedirect("/pad");
	}
}

final class SetInfStone implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("pid") == null || req.getParameter("enable") == null) {
			return;
		}
		PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
		if ("1".equals(req.getParameter("enable"))) {
			settings.setInfStone(true);
		} else if ("0".equals(req.getParameter("enable"))) {
			settings.setInfStone(false);
		}
		if (req.getParameter("ajax") == null) {
			resp.sendRedirect("/pad");
		} else {
			resp.setContentType("application/json");
			resp.getWriter().print(settings.isInfStone()?"true":"false");
		}
	}
}

final class ChgDungeonMode implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getParameter("pid") == null || req.getParameter("mode") == null) {
			return;
		} else {
			PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
			settings.setDungeonMode(Integer.parseInt(req.getParameter("mode")));
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
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PadEmulatorSettings settings;
		settings =  new PadEmulatorSettings(req.getParameter("pid"));
		if (req.getParameterValues("egg")!=null) {
			settings.addWantedEgges(req.getParameterValues("egg"));
		}
		if (req.getParameter("clean")!=null) {
			settings.cleanWantedEggs();
		}
		if (req.getParameter("mode")!=null) {
			settings.setLookingForCertainEgg(Integer.parseInt(req.getParameter("mode")));
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
				settings.setBlockLevelUp(false);
			} else {
				settings.setBlockLevelUp(true);
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
		LogList ll;
		ll = PadEmulatorSettings.log();
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
				settings.agentOn(true);
			} else if (req.getParameter("on").equals("false")) {
				settings.agentOn(false);
			}

			if (req.getParameter("ajax") == null) {
				resp.sendRedirect("/pad");
			} else {
				resp.setContentType("application/json");
				resp.getWriter().print(settings.agentOn()?"true":"false");
			}
		}
	}
}
