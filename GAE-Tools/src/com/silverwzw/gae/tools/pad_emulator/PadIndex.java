package com.silverwzw.gae.tools.pad_emulator;

import com.silverwzw.gae.ActionRouterServlet;

@SuppressWarnings("serial")
public final class PadIndex extends ActionRouterServlet {

	public PadIndex() {
		setAction("showDungeon", new ShowDungeon());
		setAction("doNotLvlUp", new NoLvlUp());
		setAction("lookForEggs", new LookForEggs());
		setDefaultAction(new controlPanel());
	}
}
