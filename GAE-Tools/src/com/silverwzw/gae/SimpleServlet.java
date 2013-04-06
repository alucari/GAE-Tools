package com.silverwzw.gae;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public abstract class SimpleServlet extends HttpServlet {
	protected HttpServletRequest req;
	protected HttpServletResponse resp;
	public abstract void serv() throws IOException;
	public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		this.req = req;
		this.resp = resp;
		serv();
	}
	public final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		this.req = req;
		this.resp = resp;
		serv();
	}
}
