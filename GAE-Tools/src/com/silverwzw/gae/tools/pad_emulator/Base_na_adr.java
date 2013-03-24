package com.silverwzw.gae.tools.pad_emulator;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverwzw.gae.SimpleServlet;


@SuppressWarnings("serial")
final public class Base_na_adr extends SimpleServlet{
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		BufferedReader reader;
		HttpURLConnection conn;
		String line,str;
		
		resp.setContentType("application/json");
		
		conn = (HttpURLConnection) (new URL("http://patch-na-pad.gungho.jp/base-na-adr.json")).openConnection();
		conn.connect();
		reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		
		
		if ((line = reader.readLine()) != null) {
			str = line;
		} else {
			str = "";
		}
		while ((line = reader.readLine()) != null) {
			str += '\n' + line;
		}
		
		resp.getWriter().print(str.replaceAll("api-na-adr-pad\\.gungho\\.jp","tools\\.silverwzw\\.com"));
	}
}
