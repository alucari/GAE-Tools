package com.silverwzw.gae.tools.pad_emulator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.apache.commons.codec.binary.Hex;

final public class Channel {

	final static String hash() {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			assert false : "NoSuchAlgorithmException";
			return null;
		}
		digest.update((UserServiceFactory.getUserService().getCurrentUser().getUserId() + "silverwzw-Anti-Rainbow-Table-Salt").getBytes());
		return new String(Hex.encodeHex(digest.digest()));
	}
	final static void broadcast(String json) {
		for (String clientID : PadEmulatorSettings.userMapGoogle.keySet()) {
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
			user = PadEmulatorSettings.userMapGunghoPid.get(req.getParameter("pid"));
		} else {
			user = PadEmulatorSettings.userMapGunghoUid.get(req.getParameter("u"));
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
}
