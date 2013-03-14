package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.ActionHandler;

public class LookForEggs extends ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PadEmulatorSettings settings;
		settings =  new PadEmulatorSettings(req.getParameter("pid"));
		if (req.getParameter("release")!=null) {
			settings.releaseSaveLock();
		}
		if (req.getParameter("egg")!=null) {
			settings.addWantedEgges(req.getParameter("egg"));
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
		resp.sendRedirect("/pad");
	}

}
