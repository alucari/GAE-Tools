package com.silverwzw.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * SimpleServlet extends HttpServlet<br />
 * , which merge the handler of POST and GET method into one serv() method
 * @author Silverwzw
 * @see javax.servlet.http.HttpServlet
 */
@SuppressWarnings("serial")
public abstract class SimpleServlet extends HttpServlet implements Servable{
	/**
	 * serv method is a handler for both POST and GET method 
	 * @param req
	 * the HTTP Request object that represents the request corresponds to this call
	 * @param resp
	 * the object represents the response to this HTTP request.
	 * @throws java.io.IOException
	 */
	protected String method;
	public abstract void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException;
	public final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		method = "GET";
		serv(req,resp);
	}
	public final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		method = "POST";
		serv(req,resp);
	}
}
