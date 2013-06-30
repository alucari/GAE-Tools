package com.silverwzw.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servable is an interface that contains the method serv() which can handle one HTTP request.
 * @author Silverwzw
 */
public interface Servable {
	/**
	 * serv method is a method that handles a HTTP reuest
	 * @param req
	 * the HTTP Request object that represents the request corresponds to this call
	 * @param resp
	 * the object represents the response to this HTTP request.
	 * @throws java.io.IOException
	 */
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
