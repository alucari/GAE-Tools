package com.silverwzw.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SimpleActionHandler implements ActionHandler {
	protected HttpServletRequest req;
	protected HttpServletResponse resp;
	public final void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		this.req = req;
		this.resp = resp;
		serv();
	}
	protected abstract void serv() throws IOException;
}
