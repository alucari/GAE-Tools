package com.silverwzw.gae.tools.pad_emulator;


import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.silverwzw.gae.tools.pad_emulator.PadEmulatorSettings.LockEntry;

final public class Channel {


	final static void broadcastByWebUser(String json) {
		for (String clientID : PadEmulatorSettings.googleSet()) {
			if (PadEmulatorSettings.adminGoogleSet.contains(clientID) || clientID.equals(PadEmulatorSettings.currentUserHash())) {
				notifyByHash(clientID, json);
			}
		}
	}
	final static void broadcastById(String id, String json) {
		for (String clientID : PadEmulatorSettings.googleSet()) {
			if (PadEmulatorSettings.adminGoogleSet.contains(clientID) || clientID.equals(PadEmulatorSettings.instance(id).userInfo.getHash())) {
				notifyByHash(clientID, json);
			}
		}
	}
	final static void notifyByPid(String pid, String json) {
		ChannelServiceFactory.getChannelService().sendMessage(new ChannelMessage(PadEmulatorSettings.instance(pid).userInfo.getHash(), json));
	}
	final static void notifyByHash(String hash, String json) {
		ChannelServiceFactory.getChannelService().sendMessage(new ChannelMessage(hash, json));
	}
	final static String req2json(HttpServletRequest req) {
		String action;
		String user;
		String parameter;
		
		action = req.getParameter("action");
		parameter = "null";
		
		if (req.getParameter("pid") != null) {
			user = PadEmulatorSettings.instance(req.getParameter("pid")).userInfo.getName();
		} else {
			user = PadEmulatorSettings.instance(req.getParameter("u")).userInfo.getName();
		}
		
		if (action.equals("sneak_dungeon")) {
			parameter = req.getParameter("dung");
		}
		return "{\"type\":\"newAction\",\"action\":\"" + action + "\",\"user\":\"" + user + "\",\"parameter\":" + parameter + "}";
	}
	final static String refreshjson(){
		return "{\"type\":\"refresh\"}";
	}
	final static String refreshjson(String pid){
		return "{\"type\":\"refresh\",\"pid\":\"" + pid+ "\"}";
	}
	final static String refreshbonus(String pid) {
		return "{\"type\":\"bonus\",\"pid\":\"" + pid+ "\"}";
	}
	
	@SuppressWarnings("serial")
	final public static class ChannelToken implements java.io.Serializable {
		private String _tokenString;
		private long _creation;
		private long _duration;
		ChannelToken(String hash, int time) {
			create(hash,time);
		}
		ChannelToken(String hash) {
			create(hash,720);
		}
		private void create(String hash,int time) {
			if (time < 30) {
				time = 720;
			}
			_tokenString = ChannelServiceFactory.getChannelService().createChannel(hash,time);
			_creation = System.currentTimeMillis();
			_duration = time;
		}
		public String tokenString() {
			return _tokenString;
		}
		public long creation() {
			return _creation;
		}
		public long duration() {
			return _duration;
		}
		public boolean expired() {
			return System.currentTimeMillis() > _creation + (_duration-3) *60 *1000;
		}
	}
	

	public static ChannelToken channelToken(String hash) {
		ChannelToken token;
		token = PadEmulatorSettings.ChannelToken.get(hash);
		if(token != null && !token.expired()) {
			return token;
		}
		return forceChannelCreation(hash);
	}
	public static ChannelToken forceChannelCreation(String hash) {
		ChannelToken token;
		token = new ChannelToken(hash);
		PadEmulatorSettings.ChannelToken.set(hash,token);
		return token;
	}
	public static void broadcastLock(String pid, LockEntry lockEntry) {
		String s;
		s = "{\"type\":\"lockUpdate\", \"pid\":\"" + pid + "\",\"time\":" + lockEntry.releaseTime() + ",\"count\":" + lockEntry.lockDownCount() + ",\"now\":" + System.currentTimeMillis() + "}";
		broadcastById(pid, s);
	}
}
