package com.silverwzw.gae.tools.pad_emulator;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.servlet.SimpleServlet;


@SuppressWarnings("serial")
final public class Proxy extends SimpleServlet{
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		BufferedReader reader;
		HttpURLConnection conn;
		String line,str,host;
		Enumeration<?> enumer;
		
		enumer = req.getHeaderNames();
		str = req.getRequestURI();
		if (str.equals("/base.json") || str.equals("/pad.json")) {
			host = "http://patch-pad.gungho.jp";
		} else {
			host = "http://patch-na-pad.gungho.jp";
		}
		
		str += (req.getQueryString()==null?"":("?"+req.getQueryString()));
		
		conn = (HttpURLConnection) (new URL(host + str)).openConnection();
		
		while ( enumer.hasMoreElements() ) {
			String key = (String)enumer.nextElement();
			conn.setRequestProperty(key, req.getHeader(key));
		}
		
		if ("POST".equals(req.getMethod())) {
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
		
		conn.setRequestMethod(req.getMethod());
		conn.connect();
		reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		

		
		str = "";
		if ((line = reader.readLine()) != null) {
			str = line;
		} else {
			str = "";
		}
		while ((line = reader.readLine()) != null) {
			str += '\n' + line;
		}

		resp.setContentType(conn.getContentType());
		
		resp.getWriter().print(str.replaceAll("api-(?:na-)?(?:adr-)?pad\\.gungho\\.jp","tools\\.silverwzw\\.com"));
	}
}
