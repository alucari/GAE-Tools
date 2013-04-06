package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.apache.commons.codec.binary.Hex;

import com.silverwzw.servlet.ActionHandler;
import com.silverwzw.servlet.ActionRouterServlet;

@SuppressWarnings("serial")
public final class PadIndex extends ActionRouterServlet {
	static ArrayList<String> userList;
	static {
		userList = new ArrayList<String>();
		userList.add("cbf9d8da00cdc95dcd017fe07028029f"); //silverwzw
		userList.add("36795a4756f4b90fac03d4dd82b28db4"); //tea
		userList.add("361d39b1af4fa514bd48e43ad0bdcf0d"); //x
	}
	public PadIndex() {
		setAction("showDungeon", new ShowDungeon());
		setAction("doNotLvlUp", new NoLvlUp());
		setAction("lookForEggs", new LookForEggs());
		setAction("dungeonMode", new ChgDungeonMode());
		setAction("getJSON", new GetJSON());
		setAction("resetEggList", new ResetEggList());
		setAction("infStone", new SetInfStone());
		setAction("showLog", new ShowLog());
		setDefaultAction(new controlPanel());
	}
	public boolean preServ(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
		digest.update((UserServiceFactory.getUserService().getCurrentUser().getUserId() + "silverwzw-Anti-Rainbow-Table-Salt").getBytes());
		String hash = new String(Hex.encodeHex(digest.digest()));
		if (!userList.contains(hash)) {
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
		if (req.getParameter("release")!=null) {
			settings.releaseSaveLock();
		}
		if (req.getParameterValues("egg")!=null) {
			settings.addWantedEgges(req.getParameterValues("egg"));
		}
		if (req.getParameter("clean")!=null) {
			settings.cleanWantedEggs();
		}
		if (req.getParameter("start")!=null) {
			settings.setLookingForCertainEgg(true);
		}
		if (req.getParameter("stop")!=null) {
			settings.setLookingForCertainEgg(false);
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