package com.silverwzw.gae.tools.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;

import com.silverwzw.servlet.ActionHandler;
import com.silverwzw.servlet.ActionRouterServlet;

@SuppressWarnings("serial")
public class Log extends ActionRouterServlet {
	public Log() {
		ActionHandler ah;
		ah = new WriteActionHandler();
		setDefaultAction(ah);
		setAction("write",ah);
		setAction("read",new ReadActionHandler());
		setAction("delete",new DeleteActionHandler());
	}
	private class WriteActionHandler implements ActionHandler {
		public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			String kind;
			Entity log;
			Map<?, ?> pmap;
			kind = req.getParameter("kind"); 
			
			if (kind == null) {
				throw new KindNotFoundException();
			}
			
			log = new Entity(kind);

			pmap = req.getParameterMap();
			for (Map.Entry<?, ?> entry: pmap.entrySet()) {
				String k;
				k = (String) entry.getKey();
				if (k.equals("kind")) {
					continue;
				}
				log.setProperty(k, ((String[]) entry.getValue())[0]);;
			}
			if (req.getParameter("timestamp")==null) {
				log.setProperty("timestamp", System.currentTimeMillis());
			}
			DatastoreServiceFactory.getDatastoreService().put(log);
		}
	};
	private class ReadActionHandler implements ActionHandler {
		public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			String kind;
			Query q;
			PreparedQuery pq;
			boolean isfirstline;
			ArrayList<String> headline;

			kind = req.getParameter("kind"); 
			if (kind == null) {
				throw new KindNotFoundException();
			}
			
			q = new Query(kind);
			headline = new ArrayList<String>();

			resp.setContentType("text/csv");
			resp.addHeader("Content-Disposition", "attachment;filename=log.csv");
			
			if (req.getParameter("last") != null) {
				try {
					q.setFilter(new Query.FilterPredicate(("timestamp"), FilterOperator.GREATER_THAN, System.currentTimeMillis() - Long.parseLong(req.getParameter("last"))));
				} catch(NumberFormatException e) {
					; // if Number Format Exception, do nothing, use default query
				}
			}
			
			pq = DatastoreServiceFactory.getDatastoreService().prepare(q.addSort("timestamp", SortDirection.DESCENDING));
			
			isfirstline = true;
			
			for (Entity ent : pq.asIterable()) {
				Map<String, Object> mp;	
				String line;
				
				mp = ent.getProperties();
				
				if (isfirstline) {
					
					line = "";
					
					for(String k : mp.keySet()) {
						headline.add(k);
					}
					for (String k : headline) {
						line += k + ",";
					}
					resp.getWriter().println(line);
					isfirstline = false;
				}
				
				line = "";
				
				for(int i = 0; i < headline.size(); i++) {
					Object o;
					String s;
					
					o =  ent.getProperty(headline.get(i));
					if (o instanceof String) {
						s = (String) o;
					} else {
						s = o.getClass().cast(o).toString();
					}
					line += "\"" + s + "\",";
				}
				resp.getWriter().println(line);
			}
		}
	};
	private class DeleteActionHandler implements ActionHandler {
		public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			if (!com.google.appengine.api.users.UserServiceFactory.getUserService().isUserAdmin()) {
				throw new DeleteOpertaionCalledByNonAdmin();
			}
			String kind;
			LinkedList<Key> llk;
			kind = req.getParameter("kind"); 
			if (kind == null) {
				throw new KindNotFoundException();
			}
			llk = new LinkedList<Key>();
			for (Entity ent : DatastoreServiceFactory.getDatastoreService().prepare(new Query(kind)).asIterable()) {
				llk.add(ent.getKey());
			}
			DatastoreServiceFactory.getDatastoreService().delete(llk);
		}
	};
	public class KindNotFoundException extends RuntimeException {};
	public class DeleteOpertaionCalledByNonAdmin extends RuntimeException {};
}
