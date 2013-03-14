package com.silverwzw.gae.tools;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;


@SuppressWarnings("serial")
public class Jmp extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
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
			String j = req.getParameter("j"); 
			if (j != null && j.equals("1")) {
				resp.setHeader("location", (String) urlMap.getProperty("url"));
				resp.setStatus(302);
				return;
			} else {
				resp.getWriter().print((String) urlMap.getProperty("url"));
				return;
			}
		} catch (EntityNotFoundException e) {
			resp.sendError(404, "Not Found");
			return;
		}
		
	}
}
