package com.silverwzw.gae.tools;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class test extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
	}
}
/*public class test extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
			PreparedQuery pq;
			Pattern d = Pattern.compile("(13-01-[0-9]{2}).(\\d{1,2}:\\d{1,2}:\\d{1,2})\\[([a-zA-Z,\\s]+)\\](INIT)?");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
			DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
			
			pq = ds.prepare(new Query("herlog"));
			resp.setContentType("text/plain");
			for (Entity enti : pq.asIterable()) {
				Matcher m = d.matcher((String)enti.getProperty("content"));
				while (m.find()) {
					Date date;
					Entity ent;
					try {
						date = df.parse("20"+m.group(1) + " " + m.group(2) + " " + "EST");
					} catch (ParseException e) {
						throw new RuntimeException("date parse error");
					}
					ent = new Entity("NicoleLog");
					ent.setProperty("initial", "0");
					ent.setProperty("timestamp", date.getTime());
					ent.setProperty("status", m.group(3));
					ent.setProperty("time", m.group(1) + " " + m.group(2));
					ds.put(ent);
					resp.getWriter().print("Date=" + m.group(1) + " Time=" + m.group(2) + " Status=" + m.group(3) + " put OK\n");
				}
			}
	}

}
*/