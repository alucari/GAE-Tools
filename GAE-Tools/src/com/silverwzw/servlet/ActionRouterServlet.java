package com.silverwzw.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ActionRouterServlet extends SimpleServlet<br />
 * it route the HTTP request to different handler based on the "action" parameter in the URL query string.<br /><br />
 * get the name of action by calling getAction()<br />
 * Specify handler using setAction()<br />
 * Specify default handler using setDefaultAction()<br />
 * Specify pre-route condition/action by overriding preServ()<br />
 * Specify post-route action by overriding postServ()<br />
 * Specify Universal Exception handler by overriding onException()<br />
 * @author Silverwzw
 * @see com.silverwzw.servlet.SimplerServlet
 * @see com.silverwzw.servlet.ActionHandler
 */
@SuppressWarnings("serial")
public abstract class ActionRouterServlet extends SimpleServlet {
	
	private HashMap<String,ActionHandler> _ARS_actionHandlers = new HashMap<String,ActionHandler>();
	private ActionHandler _ARS_defaultHandler = null;
	private static Pattern _ARS_pattern;
	static {
		_ARS_pattern = Pattern.compile("^(?:.*?&)??action=([^&]+)");
	}
	/**
	 * serv method is a final method response for routing request to handlers based on the Action parameter specified in query string.<br /><br />
	 * serv will call preServ prior to routing, if preServ() returns false, serv() will directly return HTTP 400 and stop immediately.<br /><br />
	 * serv will then check the action parameter, if:<br />
	 * * action handler is found, route the request to that ActionHandler<br />
	 * * no action is specified, and default handler exists, route to default handler.<br />
	 * * in all other case, return HTTP 400 and stop immediately.<br /><br />
	 * After the action handler returns, postServ() method will be called.
	 * @param req
	 * the HTTP Request object that represents the request corresponds to this call
	 * @param resp
	 * the object represents the response to this HTTP request.
	 * @throws java.io.IOException
	 */
	final public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ActionHandler ah;
		try {
			if (!preServ(req,resp)) {
				resp.sendError(400, "pre-condition in ActionRouter not meet");
				return;
			}
			String action;
			action = getAction(req); 
			if (action != null && _ARS_actionHandlers.containsKey(action)) {
				ah = _ARS_actionHandlers.get(action);
			} else if (_ARS_defaultHandler != null) {
				ah = _ARS_defaultHandler;
			} else {
				resp.sendError(400, "no such action handler");
				return;
			}
			
			ah.serv(req, resp);
		
			postServ(req,resp);
		} catch (Exception e) {
			if (!onException(req,resp,e)) {
				if (e instanceof IOException) {
					throw (IOException) e;
				} else if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	/**
	 * setAction method assigns handler to action
	 * @param actionName
	 * the action name (value of first "action" parameter in query string)
	 * @param ah
	 * corresponding handler
	 * @see com.silverwzw.servlet.ActionHandler
	 */
	final protected void setAction(String actionName, ActionHandler ah) {
		_ARS_actionHandlers.put(actionName, ah);
	}
	/**
	 * setDefaultAction method assigns default handler to the servlet
	 * @param defaultActionHandler
	 * the default handler, will be called when no action is specified
	 */
	final protected void setDefaultAction(ActionHandler defaultActionHandler){
		_ARS_defaultHandler = defaultActionHandler;
	}
	/**
	 * Overriding this method to custom the pre-route condition/action.
	 * @param req
	 * object that represents corresponding HTTP request
	 * @param resp
	 * object that represents the response to this HTTP request.
	 * @return whether the pre-condition is meet
	 * @throws java.io.IOException
	 */
	protected boolean preServ(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		return true;
	}
	/**
	 * Overriding this method to custom the post-route action.
	 * @param req
	 * object that represents corresponding HTTP request
	 * @param resp
	 * object that represents the response to this HTTP request.
	 * @throws java.io.IOException
	 */
	protected void postServ(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		;
	}
	/**
	 * Overriding this method to custom the universal error handler.
	 * @param req
	 * object that represents corresponding HTTP request
	 * @param resp
	 * object that represents the response to this HTTP request.
	 * @param e
	 * the Exception captured
	 * @return
	 * true => the error has been successfully handled<br />
	 * false => the error cannot be handled. Then ActionRouterServlet will try to:<br />
	 * * 1. throw it to upper level (if instance of IOException or RuntimeException)<br />
	 *   or<br />
	 * * 2.print stack and return<br />
	 * @throws java.io.IOException
	 */
	protected boolean onException(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
		return false;
	}
	/**
	 * return the name of the action
	 * @param req
	 * object that represents corresponding HTTP request
	 * @return
	 * the name of the action
	 */
	final protected static String getAction(HttpServletRequest req) {
		String action = null;
		Matcher m;
		if (req.getQueryString() != null) {
			m = _ARS_pattern.matcher(req.getQueryString());
			if (m.find()) {
				action = m.group(1);
			}
		}
		return action;
	}
}