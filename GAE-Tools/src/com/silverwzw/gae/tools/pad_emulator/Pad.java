package com.silverwzw.gae.tools.pad_emulator;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
	private static Pattern pfindid = Pattern.compile("&(?:u|pid)=([0-9A-Fa-f\\-]+)");
	private static Pattern pstamax = Pattern.compile("\"sta_max\":(\\d+)");
	private static Pattern pstatime = Pattern.compile("\"sta_time\":\"(\\d+)\"");
	private static Pattern psuperfriendcard,psuperfriendplus;
	private static Pattern presError = Pattern.compile("\\{\"res\":[^0]\\d*\\}");
	
	static {
		String pids,reg;
		Iterator<String> iter;
		iter = PadEmulatorSettings.pidSet().iterator();
		pids = "(?:" + iter.next();
		while (iter.hasNext()) {
			pids += "|" + iter.next();
		}
		pids += ")";
		reg = "(?<=\\{\"v\":\\d,\"pid\":" + pids + ",\"name\":\"[^\"]{0,60}\",\"lv\":\\d{1,3},\"card\":)\\d+,\"clv\":\\d+(?=,\"slv\":\\d+,\"at\":\\d+,\"acctime\":\"\\d+\",\"plus\":\\[\\d{1,2},\\d{1,2},\\d{1,2},\\d{1,2}\\],\"fri\":\\d+,\"friMax\":\\d+\\})";
		psuperfriendcard = Pattern.compile(reg);
		reg = "(?<=\\{\"v\":\\d,\"pid\":" + pids + ",\"name\":\"[^\"]{0,60}\",\"lv\":\\d{1,3},\"card\":\\d{1,3},\"clv\":\\d{1,2},\"slv\":\\d{1,2},\"at\":\\d{1,2},\"acctime\":\"\\d{12}\",\"plus\":\\[)\\d{1,2},\\d{1,2},\\d{1,2}(?=,\\d{1,2}\\],\"fri\":\\d+,\"friMax\":\\d+\\})";
		psuperfriendplus = Pattern.compile(reg);
	}
	
	final static class NoIdFoundException extends RuntimeException{
		NoIdFoundException(){};
		NoIdFoundException(String s){super(s);};
		NoIdFoundException(Exception e){super(e);};
	};
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		if (PadEmulatorSettings.isSystemLockDown()) {
			resp.getWriter().print("{\"res\":98}");
			return;
		}
		
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

		if (req.getParameter("pid") != null) { 
			settings = new PadEmulatorSettings(req.getParameter("pid"));
		} else {
			settings = new PadEmulatorSettings(req.getParameter("u"));
		}
		
		Channel.broadcastByWebUser(Channel.req2json(req));
		if (!PadEmulatorSettings.isAdmin(settings.userInfo.getHash())) {
			Channel.notifyByPid(settings.userInfo.getPid(), Channel.req2json(req));
		}

		resp.setHeader("Content-Type", "text/html; charset=UTF-8");
		
		if (actionIs("login", req) && !settings.agentOn.does()) {
			settings.loginString.set(req.getQueryString());
		}
		
		if (actionIs("confirm_level_up",req)) {
			if (settings.blockLvlUp.does()) {
				resp.getWriter().print("{\"res\":97}");
				return;
			} else {
				settings.blockLvlUp.set(true);
			}
		}
		
		if (actionIs("do_continue",req) && settings.resolve.isActive()) {
			resp.getWriter().print("{\"res\":0,\"rid\":\"515dce31e0532\"}");
			return;
		}
		
		if (actionIs("do_continue_ack",req) && settings.resolve.isActive()) {
			resp.getWriter().print("{\"res\":0}");
			return;
		}
		
		if (actionIs("sneak_dungeon",req)) {
			if (settings.lastFailedTS.get().equals(req.getParameter("time"))) {
				resp.getWriter().print("{\"res\":96}");
				return;
			}
			boolean acquireRes = settings.lockEntry().acquire(); 
			Channel.broadcastLock(settings.userInfo.getPid(), settings.lockEntry());
			if (!acquireRes) {
				resp.getWriter().print("{\"res\":97}");
				return;
			}
		}
		
		try {
			conn.connect();
		} catch (IOException e){
			PadEmulatorSettings.systemLockDown();
		}
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
		PadEmulatorSettings.Log.log(urlStr, res);
		
		if (presError.matcher(res).matches()) {
			resp.getWriter().print(res);
			return;
		}
		
		if (actionIs("sneak_dungeon",req)) { //action is sneak_dungeon and server didn't response an error
			
			//save dungeon string for debug
			settings.dungeon.setDungeon(res);
			//notify the client
			Channel.notifyByHash(settings.userInfo.getHash(), "{\"type\":\"dungeon\",\"pid\":\"" + settings.userInfo.getPid() + "\",\"dungeon\":" + res + "}");

			int conditionNumber;
			// initial value, if not looking for eggs, enter_gundeon will be true(always enter dungeon )
			//otherwise false unless certain condition meet and got set to true (code below).
			conditionNumber = settings.eggHunting.getConditionNumber(); 
			
			//block the msg if egg hunting is on and not found desired egg
			//mode 1: egg list enabled
			//mode 2: egg list and plus egg enabled
			Collection<Integer> eggs;
			Map<String, Integer> huntEggs;
			
			eggs = settings.dungeon.eggs();
			PadEmulatorSettings.StatisticFunction.log(Integer.parseInt(req.getParameter("dung")), Integer.parseInt(req.getParameter("floor")), settings.dungeon.eggs(), settings.dungeon.pval());
			huntEggs = settings.eggHunting.huntEggMap();
			
			if (conditionNumber > 0 && !huntEggs.isEmpty()) {
				for (Integer egg : eggs) {
					if (huntEggs.containsKey(egg.toString())) {
						conditionNumber -= huntEggs.get(egg.toString());
					}
				}
			}
			
			if (conditionNumber > 0 && settings.eggHunting.getMode() == 2) {
				conditionNumber -= 10 * settings.dungeon.pval();
			}
	
			
			if (conditionNumber > 0) {
				settings.lastFailedTS.set(req.getParameter("time"));
				resp.getWriter().print("{\"res\":100}");
				return;
			}
			
			//set the dungeon content based on the dungeon Mode
			res = settings.dungeon.moddedDungeon();
			settings.lockEntry().full();
			Channel.broadcastLock(settings.userInfo.getPid(), settings.lockEntry());
		}
		
		//set the flag
		if (actionIs("get_player_data",req)) {
			settings.playerData.set(res);
			Matcher m = pmsg.matcher(res);
			res = m.replaceAll(",\"msg\":\"Silverwzw's P&D Cracker\\\\n");
			m = pstamax.matcher(res);
			if (m.find()) {
				settings.stamina.maxValue(m.group(1));
			}
			m = pstatime.matcher(res);
			if (m.find()) {
				settings.stamina.time2full(m.group(1));
			}
			
		}
		
		if ((actionIs("get_player_data",req) || actionIs("get_helpers",req)) && !settings.superFriend.get().equals("")) {
			Matcher m = psuperfriendcard.matcher(res);
			res = m.replaceAll(settings.superFriend.get() + ",\"clv\":99");
			m = psuperfriendplus.matcher(res);
			res = m.replaceAll("99,99,99");
		}
		
		if (actionIs("sneak_dungeon_ack",req)) {
			Matcher m;
			m = pstatime.matcher(res);
			if (m.find()) {
				settings.stamina.time2full(m.group(1));
			}
		}

		if (actionIs("download_limited_bonus_data",req)) {
			settings.dailyBonus.set(res);
		}
		if (actionIs("download_enemy_skill_data",req)) {
			settings.downloadData.saveEnemySkillData(res);
		}
		if (actionIs("download_skill_data",req)) {
			settings.downloadData.saveSkillData(res);
		}
		if (actionIs("download_dungeon_data",req)) {
			settings.downloadData.saveDungeonData(res);
		}
		if (actionIs("download_card_data",req)) {
			settings.downloadData.saveCardData(res);
		}
		resp.getWriter().print(res);
	}
	final private static boolean actionIs(String actionName,HttpServletRequest req) {
		return req.getParameter("action").equals(actionName);
	}
	final private static String agent(String qs) {
		String id, decorator;
		PadEmulatorSettings.Agent agent;
		PadEmulatorSettings settings;
		
		id = getId(qs);
		settings = new PadEmulatorSettings(id);
		
		agent = PadEmulatorSettings.detectActiveAgentByQueryString(qs);
		if (agent != null) {
			qs = agent.agentString();
		}

		decorator = "";
		decorator += settings.userInfo.regionIsUS() ? "-na":"";
		decorator += settings.userInfo.devIsApple() ? "":"-adr";
		
		return "http://api" + decorator + "-pad.gungho.jp/api.php" + "?" + qs;
	}
	final private static String getId(String qs) {
		Matcher m = pfindid.matcher(qs);
		if (!m.find()) {
			throw new NoIdFoundException();
		}
		return m.group(1);
	}
}