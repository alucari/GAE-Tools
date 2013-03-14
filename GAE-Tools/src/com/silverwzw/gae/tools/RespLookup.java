package com.silverwzw.gae.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;

import javax.servlet.http.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class RespLookup extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String method, encode, respType;
		String res;
		boolean send, noContent;
		
		send = false;
		noContent = false;
		res = "";
		
		for (Object argName : req.getParameterMap().keySet()) {
			if ("no-content".equals((String)argName)) {
				noContent = true;
			} else {
				if (req.getParameter(((String)argName)).equals("")) {
					resp.sendError(400, (String) argName + " not specified");
				}
			}
		}
		
		if (req.getParameter("u") == null) {
			resp.sendError(400, "url not specified");
		}
		
		if (req.getParameter("m") == null) {
			method = "GET";
		} else {
			method = req.getParameter("m");
		}
		
		if (req.getParameter("c") == null) {
			respType = "text/plain";
		} else {
			respType = req.getParameter("c");
		}
		
		if (req.getParameter("e") == null) {
			encode = "UTF-8";
		} else {
			encode = req.getParameter("e");
		}
		
		try {
			URL url = new URL(req.getParameter("u"));
			OutputStreamWriter writer;
			
			writer = null;
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			for (Object argName : req.getParameterMap().keySet()) {
				
				String argNameS;
				argNameS = (String) argName;
				
				if (argNameS.charAt(0) == '_') {
					
					if (send == false) { // if this is the first POST data
						send = true;
						conn.setDoOutput(true);
						writer = new OutputStreamWriter(conn.getOutputStream());
					} else {
						writer.write("&");
					}
					
					writer.write(argNameS.substring(1) + "=" + URLEncoder.encode(req.getParameter(argNameS), encode));
					
				} else {
					if (argNameS.equals("no-content") || argNameS.equals("c") || argNameS.equals("m") || argNameS.equals("u") || argNameS.equals("e")) {
						continue;
					}
					conn.setRequestProperty(argNameS, req.getParameter(argNameS));
				}
			}
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod(method);
			
			if (send) {
				writer.close();
			}
			
			conn.connect();
			
			resp.setContentType(respType);
			
			res += "Response Code:\n\t" + conn.getResponseCode() + "\n\n";
			
			res +=  "Response Header:\n";
			
			Map<String, List<String>> header;
			header = conn.getHeaderFields();
			for (String fieldName : header.keySet()) {
				res += "\t" + fieldName + ":\n";
				for (String fieldValue : header.get(fieldName)) {
					res += "\t\t" + fieldValue + ",\n";
				}
			}
			
			if (!noContent) {
				res += "\nResponse Content:\n";
			
				String line;
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				while ((line = reader.readLine()) != null) {
					res += line + '\n';
				}
			}
			resp.getWriter().print(res);
			
		} catch (MalformedURLException e) {
			resp.sendError(400, "URL Error, check the parameter u");
		}
	}
}