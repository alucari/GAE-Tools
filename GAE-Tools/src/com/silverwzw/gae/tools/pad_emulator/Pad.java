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
	private PadEmulatorSettings settings;
	private static Pattern pattern = Pattern.compile(",\\s*\"msg\"\\s*:\"");
	public void serv() throws IOException {

		String target;
		
		target = isApple()?"http://api-na-pad.gungho.jp/api.php":"http://api-na-adr-pad.gungho.jp/api.php";
		
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
		
		while ( enumer.hasMoreElements() ) {
			String key;
			key = (String)enumer.nextElement();
			c2sHeader.put(key, req.getHeader(key));
		}
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
		
		if (actionIs("do_continue") && settings.isInfStone()) {
			resp.getWriter().print("{\"res\":0,\"rid\":\"515dce31e0532\"}");
			return;
		}
		
		if (actionIs("do_continue_ack") && settings.isInfStone()) {
			resp.getWriter().print("{\"res\":0}");
			return;
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
		PadEmulatorSettings.log(req.getQueryString(), res);
		if (actionIs("sneak_dungeon")) {
			(new PadEmulatorSettings(req.getParameter("pid"))).setDungeonString(res);
			Dungeon dungeon;
			dungeon = new Dungeon(res);

			if (settings != null && settings.getDungeonMode()>0) {
				res = dungeon.modDungeon(settings.getDungeonMode());
			}
		}
		if (actionIs("get_player_data")) {
			Matcher m = pattern.matcher(res);
			res = m.replaceAll(",\"msg\":\"Silverwzw-");
		}
		resp.getWriter().print(res);
	}
	private boolean actionIs(String actionName) {
		return req.getParameter("action").equals(actionName);
	}
	private boolean isApple(){
		String qs = req.getQueryString();
		if (qs.indexOf("dev=iPad3,4") >= 0) {
			return true;
		}
		if (qs.indexOf("dev=iPhone5,1") >= 0) {
			return true;
		}
		if (qs.indexOf("pid=324151024")>=0) {
			return true;
		}
		if (qs.indexOf("pid=324224887")>=0) {
			return true;
		}
		return false;
	}
}
