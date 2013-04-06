package com.silverwzw.gae.tools.cc98;

import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Collections;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.silverwzw.servlet.SimpleServlet;

@SuppressWarnings("serial")
public class CC98top10 extends SimpleServlet {
	private Cache cache;
	public void serv() throws IOException {
		try {
			String contentType;
			String rssText;
			
			cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			contentType="application/rss+xml";
			
			if (req.getParameter("boardid") != null) {
				bypass(req.getParameter("boardid"),req.getParameter("id"));
				return;
			}
			
			if (req.getHeader("If-Modified-Since") != null && isRssCacheVaild()) {
				long lm;
				lm = (Long) cache.get("rssTime");
				if (req.getDateHeader("If-Modified-Since") > lm && lm > System.currentTimeMillis() - 900000) {
					resp.setStatus(304);
					return;
				}
			}
			
			if (req.getParameter("content-type") != null) {
				contentType = req.getParameter("content-type");
			}
			
			if (req.getParameter("reset") != null) {
				cache.put("rssTime", (long)0);
			}
			
			contentType += "; charset=utf-8";
			
			rssText = getRss();
			if (isRssCacheVaild() && !"".equals(rssText)) {
				resp.addDateHeader("expires", 1200000 + (Long) cache.get("rssTime"));
			}
			
			resp.setContentType(contentType);
			resp.getWriter().print(rssText);
			
		} catch (MalformedURLException e) {
			resp.sendError(500, "Malformed URL Exception.");
			return;
		} catch (CacheException e) {
			resp.sendError(500, "Cache Exception Captured.");
			return;
		}
	}
	private String getRss() throws MalformedURLException, IOException {
		if (isRssCacheVaild()) {
			return (String) cache.get("rssText");
		} else {
			URL url;
			String line;
			BufferedReader reader;
			String rss;
			
			rss = "";
			url = new URL(SecretTunnelURL.get());
			line = "";
			
			try {
				reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
			} catch (IOException e) {
				if (cache.containsKey("rssText")) {
					return (String) cache.get("rssText");
				} else {
					return "";
				}
			}
			
			
			while ((line = reader.readLine()) != null) {
				rss += line;
			}
			
			rss = rss.replace("<link><![CDATA[http://www.cc98.org/dispbbs.asp?","<link><![CDATA[http://tools.silverwzw.com/cc98top10?");
			rss = rss.replace("<guid><![CDATA[http://www.cc98.org/dispbbs.asp?", "<guid><![CDATA[http://tools.silverwzw.com/cc98top10?");
			rss = rss.replace("<link>http://www.cc98.org</link>","<link>http://tools.silverwzw.com</link>");
			rss = rss.replaceAll("\\[upload.*?\\]", "").replaceAll("\\[/upload\\]", "");
			rss = rss.replaceAll("\\[font=.*?\\]", "").replaceAll("\\[/font\\]", "");
			rss = rss.replaceAll("\\[size=.*?\\]", "").replaceAll("\\[/size\\]", "");
			rss = rss.replaceAll("\\[color=.*?\\]", "").replaceAll("\\[/color\\]", "");
			rss = rss.replaceAll("\\[align=.*?\\]", "").replaceAll("\\[/align\\]", "");
			cache.put("rssText", rss);
			cache.put("rssTime", System.currentTimeMillis());
			return rss;
		}
	}
	
	private boolean isRssCacheVaild() {
		if (cache.containsKey("rssText") && cache.containsKey("rssTime")) {
			if ((Long) cache.get("rssTime") < System.currentTimeMillis() - 900000) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	private void bypass(String bid, String id) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		String add;
		boolean isad = false;
		
		if (userService.isUserLoggedIn()) {
			if(userService.isUserAdmin()) {
				isad=true;
			}
		}
		if(isad) {
			add = "http://www.isee.zju.edu.cn/px/index.php?q=http%3A%2F%2Fwww.cc98.org%2Fdispbbs.asp%3Fboardid%3D"+bid+"%26id%3D"+id+"&hl=2e5";
		}
		else {
			add = "http://www.cc98.org/dispbbs.asp?boardid="+bid+"&id="+id;
			resp.setHeader("x-login",userService.createLoginURL("http://tools.silverwzw.com/cc98top10?boardid="+bid+"&id="+id));
		}
		resp.setHeader("location", add);
		resp.setStatus(302);
	}
}
