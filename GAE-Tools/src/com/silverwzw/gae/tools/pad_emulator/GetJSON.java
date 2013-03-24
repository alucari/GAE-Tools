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
		
		json = "{";
		json += "\"isBlockLevelUp\":" + b2s(settings.isBlockLevelUp()) + ",";
		json += "\"isLookingForCertainEgg\":" + b2s(settings.isLookingForCertainEgg()) + ",";
		eggs = (Collection<String>)settings.WantedEggs();
		json += "\"wantedEggs\":" + (eggs.size() == 0 ? "[],":"[");
		for (String egg : eggs) {
			json += egg;
			i++;
			if (i == eggs.size()) {
				json += ']';
			}
			json += ',';
		}
		json += "\"saveLock\":" + b2s(settings.isLocked()) + ',';
		json += "\"dungeonModDisable\":" + b2s(settings.isDungeonModDisabled());
		json += '}';
		resp.getWriter().print(json);
	}
	final private String b2s(boolean b) {
		return b?"true":"false";
	}

}
