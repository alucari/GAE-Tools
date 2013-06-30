package com.silverwzw.gae.tools.pad_emulator;

import com.silverwzw.servlet.ActionRouterServlet;

@SuppressWarnings("serial")
public class PublicDataPage extends ActionRouterServlet {
	public PublicDataPage() {
		setDefaultAction(new Statistics(true));
		setAction("stat", new Statistics(true));
	}
}
