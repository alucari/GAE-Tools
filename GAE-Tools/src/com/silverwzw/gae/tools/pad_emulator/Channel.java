package com.silverwzw.gae.tools.pad_emulator;


import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelServiceFactory;

final public class Channel {


	final static void broadcast(String json) {
		for (String clientID : PadEmulatorSettings.googleCollection()) {
			ChannelServiceFactory.getChannelService().sendMessage(new ChannelMessage(clientID, json));
		}
	}
	final static void notifyByPid(String pid, String json) {
		ChannelServiceFactory.getChannelService().sendMessage(new ChannelMessage((new PadEmulatorSettings(pid)).getHash(), json));
	}
	final static void notifyByHash(String hash, String json) {
		ChannelServiceFactory.getChannelService().sendMessage(new ChannelMessage(hash, json));
	}
	final static String req2json(HttpServletRequest req) {
		String action;
		String user;
		action = req.getParameter("action");
		if (req.getParameter("pid") != null) {
			user = (new PadEmulatorSettings(req.getParameter("pid"))).getName();
		} else {
			user = (new PadEmulatorSettings(req.getParameter("u"))).getName();
		}
		if (user == null) {
			user = "unknown[" + (req.getParameter("pid") != null ? req.getParameter("pid") : req.getParameter("u")) + "]";
		}
		return "{\"type\":\"newAction\",\"action\":\"" + action + "\",\"user\":\"" + user + "\"}";
	}
	final static String refreshjson(){
		return "{\"type\":\"refresh\"}";
	}
	final static String refreshjson(String pid){
		return "{\"type\":\"refresh\",\"pid\":\"" + pid+ "\"}";
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
		token = PadEmulatorSettings.getToken(hash);
		if(token != null && !token.expired()) {
			return token;
		}
		return forceChannelCreation(hash);
	}
	public static ChannelToken forceChannelCreation(String hash) {
		ChannelToken token;
		token = new ChannelToken(hash);
		PadEmulatorSettings.setToken(hash,token);
		return token;
	}
}
