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
