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

import com.silverwzw.servlet.SimpleServlet;

@SuppressWarnings("serial")
public class Pad extends SimpleServlet{ 
	private static Pattern pmsg = Pattern.compile(",\\s*\"msg\"\\s*:\"");
	private static Pattern pitem = Pattern.compile("\"item\"\\s*?:\\s*?\"(\\d+?)\"");
	private static Pattern pitemv = Pattern.compile("\"pval\"\\s*?:\\s*?[1-9]+?");
	private static Pattern pfindid = Pattern.compile("&(?:u|pid)=([0-9A-Fa-f\\-]+)");

	final static class NoIdFoundException extends RuntimeException{};
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		PadEmulatorSettings settings;
		
		
		// POST method have highest priority, should be handled before any req.getParameter call
		String urlStr;
		HttpURLConnection conn;
		HashMap<String,String> c2sHeader;
		Enumeration<?> enumer;
		
		enumer = req.getHeaderNames();
		c2sHeader = new HashMap<String,String>();
		
		urlStr = agent(req.getQueryString());
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
		
		Channel.broadcast(Channel.req2json(req));
		
		if (req.getParameter("pid") != null) { 
			settings = new PadEmulatorSettings(req.getParameter("pid"));
		} else {
			settings = null;
		}
		

		resp.setHeader("Content-Type", "text/html; charset=UTF-8");
		
		if (actionIs("confirm_level_up",req) && settings.isBlockLevelUp()) {
			resp.getWriter().print("{\"res\":97}");
			settings.setBlockLevelUp(false); //for safety
			return;
		}
		
		if (actionIs("do_continue",req) && settings.isInfStone()) {
			resp.getWriter().print("{\"res\":0,\"rid\":\"515dce31e0532\"}");
			return;
		}
		
		if (actionIs("do_continue_ack",req) && settings.isInfStone()) {
			resp.getWriter().print("{\"res\":0}");
			return;
		}
		
		if (actionIs("sneak_dungeon",req) && settings.lastFailedTS().equals(req.getParameter("time"))) {
			resp.getWriter().print("{\"res\":96}");
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
		PadEmulatorSettings.log(urlStr, res);
		
		if (actionIs("sneak_dungeon",req)) {
			
			//save dungeon string for debug
			settings.setDungeonString(res);
			//notify the client
			Channel.notifyByHash(settings.getHash(), "{\"type\":\"dungeon\",\"pid\":\"" + settings.getPid() + "\",\"dungeon\":" + res + "}");
			
			boolean enter_dungeon;
			// initial value, if not looking for eggs, enter_gundeon will be true(always enter dungeon )
			//otherwise false unless certain condition meet and got set to true (code below).
			enter_dungeon = !(settings.isLookingForCertainEgg()!=0); 
			
			//block the msg if egg hunting is on and not found desired egg
			//mode 0: nothing happens
			//mode 1: egg list enabled
			//mode 2: egg list and plus egg enabled
			if (settings.isLookingForCertainEgg() != 0  && !settings.WantedEggs().isEmpty()) { // look for eggs in list in mode 1 and 2
				Matcher mitm = pitem.matcher(settings.getDungeonString());
				while(mitm.find()) {
					if (settings.WantedEggs().contains(mitm.group(1))) {
						enter_dungeon = true;
						break;
					}
				}
			}
			if (settings.isLookingForCertainEgg() == 2) { //look for plug eggs in mode 2
				Matcher mitmpval = pitemv.matcher(settings.getDungeonString());
				if (mitmpval.find()) {
					enter_dungeon = true;
				}
			}
			
			if (!enter_dungeon) {
				settings.lastFailedTS(req.getParameter("time"));
				resp.getWriter().print("{\"res\":100}");
				return;
			}
			
			//modify the dungeon content if a dungeon Mode is specified
			
			Dungeon dungeon;
			dungeon = new Dungeon(res);

			if (settings != null && settings.getDungeonMode()>0) {
				res = dungeon.modDungeon(settings.getDungeonMode());
			}
		}
		
		//set the flag
		if (actionIs("get_player_data",req)) {
			Matcher m = pmsg.matcher(res);
			res = m.replaceAll(",\"msg\":\"Silverwzw's P&D Cracker\\\\n");
		}
		
		if (actionIs("download_limited_bonus_data",req)) {
			Bonus.saveData(req.getParameter("pid"), res);
		}
		resp.getWriter().print(res);
	}
	final private static boolean actionIs(String actionName,HttpServletRequest req) {
		return req.getParameter("action").equals(actionName);
	}
	final private static String agent(String qs) {
		String target;
		PadEmulatorSettings.Agent agent;
		
		agent = PadEmulatorSettings.detectActiveAgentByQueryString(qs);
		if (agent!=null) {
			qs = agent.agentString();
		}

		target = (new PadEmulatorSettings(getId(qs))).devIsApple() ? "http://api-na-pad.gungho.jp/api.php" : "http://api-na-adr-pad.gungho.jp/api.php";
		return target + "?" + qs;
	}
	final private static String getId(String qs) {
		Matcher m = pfindid.matcher(qs);
		if (!m.find()) {
			throw new NoIdFoundException();
		}
		return m.group(1);
	}
}