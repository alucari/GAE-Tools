package com.silverwzw.gae.tools.rss;


import com.silverwzw.servlet.ActionRouterServlet;

@SuppressWarnings("serial")
public class Rss extends ActionRouterServlet {
	public Rss() {
		setAction("cc98",new CC98top10());
	}
}
