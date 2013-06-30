package com.silverwzw.gae.tools.jmp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import com.silverwzw.servlet.SimpleServlet;


@SuppressWarnings("serial")
public class Jmp extends SimpleServlet {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		UserService u = UserServiceFactory.getUserService();
		if(!(u.isUserLoggedIn() && u.isUserAdmin())) {
			resp.sendError(401, "Not Permitted");
			return;
		}
		
		Entity urlMap;
		
		if (req.getParameter("url") != null) {
			urlMap = new Entity("urlMap",req.getParameter("jmp"));
			urlMap.setProperty("url",(String) req.getParameter("url"));
			DatastoreServiceFactory.getDatastoreService().put(urlMap);
			return;
		}
		
		try {
			urlMap = DatastoreServiceFactory.getDatastoreService().get(KeyFactory.createKey("urlMap", req.getParameter("jmp")));
			if (req.getParameter("j") != null) {
				resp.sendRedirect((String) urlMap.getProperty("url"));
			} else {
				resp.getWriter().print((String) urlMap.getProperty("url"));
			}
		} catch (EntityNotFoundException e) {
			resp.sendError(404, "Not Found");
		}
	}
}
