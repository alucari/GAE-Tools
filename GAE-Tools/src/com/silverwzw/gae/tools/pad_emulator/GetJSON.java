package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.Collection;

import com.silverwzw.servlet.SimpleActionHandler;

public final class GetJSON extends SimpleActionHandler {

	final public void serv() throws IOException {
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
		json += "\"safeLock\":" + b2s(settings.isLocked()) + ",\"dungeonMode\":" + settings.getDungeonMode() + ",\"infStone\":" + b2s(settings.isInfStone()) + '}';
		resp.getWriter().print(json);
	}
	final private String b2s(boolean b) {
		return b?"true":"false";
	}
}
