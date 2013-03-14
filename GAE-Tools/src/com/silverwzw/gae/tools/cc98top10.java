package com.silverwzw.gae.tools;

import java.io.IOException;
import javax.servlet.http.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Collections;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheManager;

import java.util.regex.*;
import java.util.ArrayList;

import com.google.appengine.api.urlfetch.*;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings({ "serial", "unused" })
public class cc98top10 extends HttpServlet {
	private Cache cache;
	private static Pattern imgSrc = Pattern.compile(".*?<(?:img|IMG).*?\\s(?:src|SRC)=\"?http://file\\.cc98\\.org/uploadfile/([0-9/]+?\\.[jJ][eE]?[pP][gG])\"?.*?>(.*)");
	private ArrayList<String> allowedJpg;
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			String contentType;
			String reset;
			String rssText;
			
			cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			contentType="application/rss+xml";
			
			if (req.getParameter("img") != null) {
				if (req.getHeader("If-Modified-Since") != null) {
					resp.setStatus(304);
				} else {
					imgOut(req.getParameter("img"), resp);
				}
				return;
			}
			
			if (req.getParameter("boardid") != null) {
				bypass(req.getParameter("boardid"),req.getParameter("id"),req,resp);
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
			
			reset = req.getParameter("reset");
			if (reset != null) {
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
			
			allowedJpg = new ArrayList<String>();
			
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
				rss += imageProcess(line);
			}
			
			rss = rss.replace("<link><![CDATA[http://www.cc98.org/dispbbs.asp?","<link><![CDATA[http://tools.silverwzw.com/cc98top10?");
			rss = rss.replace("<guid><![CDATA[http://www.cc98.org/dispbbs.asp?", "<guid><![CDATA[http://tools.silverwzw.com/cc98top10?");
			rss = rss.replace("<link>http://www.cc98.org</link>","<link>http://tools.silverwzw.com</link>");
			rss = rss.replaceAll("\\[upload.*?\\]", "").replaceAll("\\[/upload\\]", "");
			rss = rss.replaceAll("\\[font=.*?\\]", "").replaceAll("\\[/font\\]", "");
			rss = rss.replaceAll("\\[size=.*?\\]", "").replaceAll("\\[/size\\]", "");
			rss = rss.replaceAll("\\[color=.*?\\]", "").replaceAll("\\[/color\\]", "");
			rss = rss.replaceAll("\\[align=.*?\\]", "").replaceAll("\\[/align\\]", "");
			cache.put("allowedJpg", allowedJpg);
			cache.put("rssText", rss);
			cache.put("rssTime", System.currentTimeMillis());
			return rss;
		}
	}
	
	private boolean isRssCacheVaild() {
		if (cache.containsKey("rssText") && cache.containsKey("rssTime") && cache.containsKey("allowedJpg")) {
			if ((Long) cache.get("rssTime") < System.currentTimeMillis() - 900000) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	private String imageProcess (String rss) {
		
		if (rss != null && rss.length() >= 11) {
			Matcher matcher;
			
			matcher = imgSrc.matcher(rss);
			while (matcher != null && matcher.matches()) {
				allowedJpg.add(matcher.group(1));
				matcher = imgSrc.matcher(matcher.group(2));
			}
		}
		
		rss = rss.replaceAll("http://file\\.cc98\\.org/uploadfile/", "/cc98top10?img=");
		return rss+'\n';
	}
	
	private void bypass(String bid, String id, HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
	
	@SuppressWarnings("unchecked")
	private void imgOut(String imgKey, HttpServletResponse resp) throws IOException {
		if (!cache.containsKey("allowedJpg")) {
			resp.sendError(500, "cache reset is required.");
			return;
		}
		resp.addHeader("expires", "Thu, 31 Dec 2099 23:59:00 GMT");
		if (cache.containsKey(imgKey)) {
			resp.setContentType("image/jpeg");
			resp.getOutputStream().write((byte []) cache.get(imgKey));
			return;
		}
		if (((ArrayList<String>)cache.get("allowedJpg")).contains(imgKey)) {
			resp.setContentType("image/jpeg");
			byte[] b;
			try {
				URLConnection urlconn = (new URL("http://www.isee.zju.edu.cn/px/index.php?hl=3e5&q=http%3A//file.cc98.org/uploadfile/"+imgKey)).openConnection();
				urlconn.setConnectTimeout(20000);
				urlconn.connect();
				b = new byte[urlconn.getContentLength()];
				urlconn.getInputStream().read(b);
				//b = URLFetchServiceFactory.getURLFetchService().fetch(new URL("http://www.isee.zju.edu.cn/px/index.php?hl=3e5&q=http%3A//file.cc98.org/uploadfile/"+imgKey)).getContent();				
			} catch (IOException e) {
				resp.sendError(502, "when fetching image(s).");
				return;
			}
			cache.put(imgKey, b);
			resp.getOutputStream().write(b);
			return;
			
		} else {
			resp.sendError(403, "no longer in cache");
			return;
		}
	}
}
