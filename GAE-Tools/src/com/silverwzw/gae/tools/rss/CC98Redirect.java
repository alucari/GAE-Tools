package com.silverwzw.gae.tools.rss;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.servlet.SimpleServlet;

public class CC98Redirect extends SimpleServlet {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setStatus(301);
		resp.setHeader("Location", "http://tools.silverwzw.com/rss?action=cc98");
	}
}
