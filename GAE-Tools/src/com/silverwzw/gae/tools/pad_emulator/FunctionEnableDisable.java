package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.ActionHandler;

public class FunctionEnableDisable extends ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (req.getParameter("pid") == null) {
			return;
		} else {
			PadEmulatorSettings settings = new PadEmulatorSettings(req.getParameter("pid"));
			if (req.getParameter("enable")!=null) {
				settings.setDisableAllFunction(false);
			} else if (req.getParameter("disable")!=null){
				settings.setDisableAllFunction(true);
			}
			resp.sendRedirect("/pad");
		}
	}
}
