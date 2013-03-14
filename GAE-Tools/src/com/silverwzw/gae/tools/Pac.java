package com.silverwzw.gae.tools;


import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
final public class Pac extends HttpServlet{
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String pacStr,proxyAddr;
		resp.setContentType("application/x-ns-proxy-autoconfig");
		if (req.getParameter("proxy") != null) {
			proxyAddr = req.getParameter("proxy");
		} else {
			proxyAddr = "SOCKS 127.0.0.1:7070";
		}
		if (req.getParameter("type") != null && req.getParameter("type").equals("advance")) {
			pacStr = "function FindProxyForURL(url, host) {\n" + 
					"return \"DIRECT; " + proxyAddr + "\";\n" +
					"}";
		} else {
			pacStr = "function FindProxyForURL(url, host) {\n" +
					"return \"" + proxyAddr + ";DIRECT\";\n" +
					"}";
		}
		resp.getWriter().print(pacStr);
	}
}
