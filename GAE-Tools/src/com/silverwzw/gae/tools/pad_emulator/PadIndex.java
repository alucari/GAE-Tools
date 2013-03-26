package com.silverwzw.gae.tools.pad_emulator;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.apache.commons.codec.binary.Hex;

import com.silverwzw.gae.ActionHandler;
import com.silverwzw.gae.ActionRouterServlet;

@SuppressWarnings("serial")
public final class PadIndex extends ActionRouterServlet {
	static ArrayList<String> userList;
	static {
		userList = new ArrayList<String>();
		userList.add("cbf9d8da00cdc95dcd017fe07028029f"); //silverwzw
		userList.add("36795a4756f4b90fac03d4dd82b28db4"); //tea
	}
	public PadIndex() {
		setAction("showDungeon", new ShowDungeon());
		setAction("doNotLvlUp", new NoLvlUp());
		setAction("lookForEggs", new LookForEggs());
		setAction("functionEnableDisable", new FunctionEnableDisable());
		setAction("getJSON", new GetJSON());
		setAction("resetEggList", new ResetEggList());
		setDefaultAction(new controlPanel());
	}
	public boolean preServ(HttpServletRequest req, HttpServletResponse response) throws IOException{
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
		digest.update((UserServiceFactory.getUserService().getCurrentUser().getUserId() + "silverwzw-Anti-Rainbow-Table-Salt").getBytes());
		if (!userList.contains(new String(Hex.encodeHex(digest.digest())))) {
			response.sendError(401);
			return false;
		}
		return true;
	}
}

final class ResetEggList extends ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PadEmulatorSettings.resetFreqEgg();
		resp.sendRedirect("/pad");
	}
}