package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.servlet.ActionHandler;

public final class GetJSON implements ActionHandler {

	final public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String uid,json;
		PadEmulatorSettings settings;
		Set<Entry<String, Integer>> eggs;
		int i = 0;
		
		uid = req.getParameter("pid");
		settings = new PadEmulatorSettings(uid);
		
		resp.setContentType("application/json");
		
		if (req.getParameter("dungeon") != null) {
			if (req.getParameter("mode") == null) {
				resp.getWriter().print(settings.dungeon.getDungeonString());
			} else {
				resp.getWriter().print(AbstractDungeon.modDungeon(settings.dungeon.getDungeonString(), Integer.parseInt(req.getParameter("mode")), settings.dungeon.hash()));
			}
			return;
		}
		
		if (req.getParameter("player") != null) {
			resp.getWriter().print(settings.playerData.get());
			return;
		}

		if (req.getParameter("bonus") != null) {
			resp.getWriter().print(settings.dailyBonus.get());
			return;
		}
		
		eggs = settings.eggHunting.huntEggMap().entrySet();
		json = "{\"pid\":" + uid +",\"isUS\":" + (settings.userInfo.regionIsUS()?"true":"false") +",\"isBlockLevelUp\":" + b2s(settings.blockLvlUp.does()) + ",\"isLookingForCertainEgg\":" + settings.eggHunting.getMode() + ",\"wantedEggs\":" + (eggs.size() == 0 ? "[],":"[");
		for (Entry<String,Integer> egg : eggs) {
			json += "{\"id\":" + egg.getKey() + ",\"v\":" + egg.getValue() + "}";
			i++;
			if (i == eggs.size()) {
				json += ']';
			}
			json += ',';
		}
		json += "\"agentOn\":" + b2s(settings.agentOn.does()) + ",\"dungeonMode\":" + settings.dungeon.getMode() + ",\"infStone\":" + b2s(settings.resolve.isActive()) + ",\"sta_max\":" + settings.stamina.maxValue() + ",\"sta_time\":\"" + settings.stamina.time2full() + "\",\"superFriend\":\"" + settings.superFriend.get() + "\",\"conditionNumber\":" + settings.eggHunting.getConditionNumber()+ "}";
		resp.getWriter().print(json);
	}
	final private String b2s(boolean b) {
		return b?"true":"false";
	}
}
