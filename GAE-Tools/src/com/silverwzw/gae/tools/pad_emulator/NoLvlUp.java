package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.ActionHandler;

public class NoLvlUp extends ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (req.getParameter("pid") == null) {
			return;
		} else {
			PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
			if (req.getParameter("release")!=null) {
				settings.setBlockLevelUp(false);
			} else {
				settings.setBlockLevelUp(true);
			}
			resp.sendRedirect("/pad");
		}
	}

}
