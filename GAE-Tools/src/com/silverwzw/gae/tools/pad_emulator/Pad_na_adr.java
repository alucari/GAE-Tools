package com.silverwzw.gae.tools.pad_emulator;


import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
final public class Pad_na_adr extends HttpServlet{
	String str;
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/octet-stream");
		str = "{";
		p("\"res\":0,");
		p("\"links\":[");
		p("\t{	\"name\":\"Official Web Site\",");
		p("\t\t\"link\":\"http:\\/\\/www.gunghoonline.com\\/games\\/puzzle-dragons\\/\"");
		p("\t},");
		p("\t{	\"name\":\"Trailer\",");
		p("\t\t\"link\":\"http:\\/\\/www.youtube.com\\/watch?v=vgZFZtG8rQM\"");
		p("\t},");
		p("\t{\t\"name\":\"Combo Tips\",");
		p("\t\t\"link\":\"http:\\/\\/www.youtube.com\\/watch?v=bE9CP9Q07OU\"");
		p("\t},");
		p("\t{\t\"name\":\"Facebook\",");
		p("\t\t\"link\":\"https:\\/\\/www.facebook.com\\/zhuowei.wang\"");
		p("\t}");
		p("],");
		p("\"banner\":[");
		p("\t{\t\"start\":\"120101000000\",");
		p("\t\t\"end\":\"990101000000\",");
		p("\t\t\"height\":64,");
		p("\t\t\"link\":\"http:\\/\\/patch-pad.gungho.jp\\/banner.html\"");
		p("\t}");
		p("]");
		p("}");
		resp.getWriter().print(str);
	}
	 private final void p(String str2) {
		str = str + '\n' + str2;
	}
}
