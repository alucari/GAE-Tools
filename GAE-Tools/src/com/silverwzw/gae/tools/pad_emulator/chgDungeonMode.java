package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.ActionHandler;

public class chgDungeonMode extends ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (req.getParameter("pid") == null || req.getParameter("mode") == null) {
			return;
		} else {
			(new PadEmulatorSettings(req.getParameter("pid"))).setDungeonMode(Integer.parseInt(req.getParameter("mode")));
			resp.sendRedirect("/pad");
		}
	}
}
