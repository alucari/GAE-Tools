package com.silverwzw.gae.tools.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;

import com.silverwzw.servlet.SimpleServlet;

@SuppressWarnings("serial")
public class Log extends SimpleServlet {
	public void serv() throws IOException{
		String kind;
		kind = req.getParameter("kind"); 
		if (kind == null) {
			resp.sendError(400, "incomplete request");
			return;
		}
		if (req.getParameter("read")==null) {
			Entity log;
			Map<?, ?> pmap;
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
			return;
		} else {
			Query q;
			PreparedQuery pq;
			boolean isfirstline;
			ArrayList<String> headline;
			
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
	}

}
