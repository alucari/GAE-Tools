package com.silverwzw.gae.tools.pad_emulator;


import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
final public class Base_na_adr extends HttpServlet{
	String str;
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		str = "{\"res\":0,\"base\":\"http:\\/\\/tools.silverwzw.com\\/api.php\",\"rver\":\"4.2\",\"nextRver\":\"5.0\",\"nextRverTime\":\"130303003000\",\"maintStart\":\"120303000000\",\"maintEnd\":\"120323040000\",\"maintMsg\":\"We are performing a system maintenance.\",\"padinfo\":\"http:\\/\\/patch-na-pad.gungho.jp\\/pad-na-adr.json\",\"retrysecs\":15,\"upint\":6,\"extlist\":\"http:\\/\\/dl-na-pad.gungho.jp\\/nmon130306\"}";
		resp.getWriter().print(str);
	}
}
