package com.silverwzw.gae.tools.pad_emulator;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.SimpleServlet;

@SuppressWarnings("serial")
public class Pad extends SimpleServlet{
	private static String target = "http://api-na-adr-pad.gungho.jp/api.php"; 
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private PadEmulatorSettings settings;
	public void serv(HttpServletRequest request, HttpServletResponse response) throws IOException {
		

		resp = response;
		req = request;
		

		// POST method have highest priority, should be handled before any req.getParameter call
		String urlStr;
		HttpURLConnection conn;
		HashMap<String,String> c2sHeader;
		Enumeration<?> enumer;
		
		enumer = req.getHeaderNames();
		c2sHeader = new HashMap<String,String>();
		
		urlStr = target + '?' + req.getQueryString();
		conn = (HttpURLConnection) (new URL(urlStr)).openConnection();
		
		String method; 
		method = req.getMethod();
		conn.setRequestMethod(method);
		for (Entry<String,String> e : c2sHeader.entrySet()) {
			conn.setRequestProperty(e.getKey(), e.getValue());
		}
		
		if ("POST".equals(method)) {
			int b;
			ServletInputStream in;
			OutputStream out;
			conn.setDoOutput(true);
			in = req.getInputStream();
			out = conn.getOutputStream();
			b = in.read();
			while (b != -1) {
				out.write(b);
				b = in.read();
			}
			in.close();
			out.close();
		}
		// POST done;
		
		if (req.getParameter("pid") != null) { 
			settings = new PadEmulatorSettings(req.getParameter("pid"));
		} else {
			settings = null;
		}
		

		resp.setHeader("Content-Type", "text/html; charset=UTF-8");
		
		if (actionIs("confirm_level_up") && settings.isBlockLevelUp()) {
			resp.getWriter().print("{\"res\":97}");
			settings.setBlockLevelUp(false); //for safety
			return;
		}
		
		if (actionIs("sneak_dungeon") && settings.isLocked()) {
			resp.getWriter().print("{\"res\":98}");
			return;
		}
		
		if (actionIs("sneak_dungeon_ack") && settings.isLookingForCertainEgg() && !settings.WantedEggs().isEmpty()) {
			Matcher mitm = Pattern.compile("\"item\"\\s*?:\"(\\d+?)\"").matcher(settings.getDungeonString());
			boolean find_one_egg;
			find_one_egg = false;
			while(mitm.find()) {
				if (settings.WantedEggs().contains(mitm.group(1))) {
					find_one_egg = true;
					break;
				}
			}
			if (!find_one_egg) {
				resp.getWriter().print("{\"res\":96}");
				settings.acquireSaveLock();
				return;
			} else {
				settings.setLookingForCertainEgg(false);
			}
		}
		
		
		while ( enumer.hasMoreElements() ) {
			String key;
			key = (String)enumer.nextElement();
			c2sHeader.put(key, req.getHeader(key));
		}
		conn.connect();
		String res, line;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		if ((line = reader.readLine()) != null) {
			res = line;
		} else {
			res = "";
		}
		while ((line = reader.readLine()) != null) {
			res += '\n' + line;
		}
		
		if (actionIs("sneak_dungeon")) {
			(new PadEmulatorSettings(req.getParameter("pid"))).setDungeonString(res);
			Dungeon dungeon;
			dungeon = new Dungeon(res);
			res = dungeon.modDungeon();
		}
		if (actionIs("get_player_data")) {
			Matcher m = Pattern.compile(",\\s*\"msg\"\\s*:\"").matcher(res);
			res = m.replaceAll(",\"msg\":\"Silverwzw-");
		}
		resp.getWriter().print(res);
	}
	private boolean actionIs(String actionName) {
		return req.getParameter("action").equals(actionName);
	}
}
