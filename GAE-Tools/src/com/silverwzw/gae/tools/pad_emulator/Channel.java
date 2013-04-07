package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.apache.commons.codec.binary.Hex;
import com.silverwzw.servlet.ActionHandler;

final public class Channel {
	static HashMap<String,String> userMapGungho;
	static HashMap<String,String> userMapGoogle;
	static {
		userMapGoogle = new HashMap<String,String>();
		userMapGoogle.put("cbf9d8da00cdc95dcd017fe07028029f","silverwzw"); //silverwzw
		userMapGoogle.put("36795a4756f4b90fac03d4dd82b28db4","tea"); //tea
		userMapGoogle.put("361d39b1af4fa514bd48e43ad0bdcf0d","x"); //x
		userMapGungho = new HashMap<String,String>();
		userMapGungho.put("324151024", "tea");
		userMapGungho.put("B33ECFC8-F74D-4A88-A5D5-81183DAFC850", "tea");
		userMapGungho.put("324363124", "silverwzw");
		userMapGungho.put("0a78f1a0-f5a0-49ef-950e-e6205f5e9389", "silverwzw");
		userMapGungho.put("324224887", "x");
		userMapGungho.put("27C8DDB8-D23C-4345-94B6-805A5DD36A1F", "x");
	}
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
		for (String clientID : userMapGoogle.keySet()) {
			ChannelServiceFactory.getChannelService().sendMessage(new ChannelMessage(clientID, json));
		}
	}
	final static String req2json(HttpServletRequest req) {
		String action;
		String user;
		action = req.getParameter("action");
		if (req.getParameter("pid") != null) {
			user = userMapGungho.get(req.getParameter("pid"));
		} else {
			user = userMapGungho.get(req.getParameter("u"));
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


final class RealTimeChannel implements ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String token,clientID;
		clientID = Channel.hash();
		token = ChannelServiceFactory.getChannelService().createChannel(clientID);
		resp.setContentType("application/json");
		resp.getWriter().print("{\"token\":\"" + token + "\"}");
	}
}