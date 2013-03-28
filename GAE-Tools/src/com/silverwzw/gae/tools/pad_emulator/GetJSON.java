package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.ActionHandler;

public class GetJSON extends ActionHandler {

	final public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String uid,json;
		PadEmulatorSettings settings;
		Collection<String> eggs;
		int i = 0;
		
		uid = req.getParameter("pid");
		settings = new PadEmulatorSettings(uid);
		
		resp.setContentType("application/json");
		
		if (req.getParameter("dungeon") != null) {
			if (req.getParameter("mode") == null) {
				resp.getWriter().print(settings.getDungeonString());
			} else {
				resp.getWriter().print((new Dungeon(settings.getDungeonString())).modDungeon(Integer.parseInt(req.getParameter("mode"))));
			}
			return;
		}
		eggs = (Collection<String>)settings.WantedEggs();
		json = "{\"pid\":" + uid + ",\"isBlockLevelUp\":" + b2s(settings.isBlockLevelUp()) + ",\"isLookingForCertainEgg\":" + b2s(settings.isLookingForCertainEgg()) + ",\"wantedEggs\":" + (eggs.size() == 0 ? "[],":"[");
		for (String egg : eggs) {
			json += egg;
			i++;
			if (i == eggs.size()) {
				json += ']';
			}
			json += ',';
		}
		json += "\"safeLock\":" + b2s(settings.isLocked()) + ",\"dungeonMode\":" + settings.getDungeonMode() + '}';
		resp.getWriter().print(json);
	}
	final private String b2s(boolean b) {
		return b?"true":"false";
	}

}
