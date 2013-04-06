package com.silverwzw.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public abstract class SimpleServlet extends HttpServlet {
	public abstract void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException;
	public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		serv(req,resp);
	}
	public final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		serv(req,resp);
	}
}
