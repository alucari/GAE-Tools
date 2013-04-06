package com.silverwzw.servlet;

import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("serial")
public abstract class ActionRouterServlet extends SimpleServlet {
	
	private HashMap<String,ActionHandler> actionHandlers = new HashMap<String,ActionHandler>();
	private ActionHandler defaultHandler = null;
	
	final public void serv() throws IOException {
		
		if (!preServ()) {
			return;
		}
		String action;
		action = req.getParameter("action");
		if (action != null && actionHandlers.containsKey(req.getParameter("action"))) {
			actionHandlers.get(req.getParameter("action")).serv(req,resp);
		} else if (defaultHandler != null) {
			defaultHandler.serv(req,resp);
			return;
		} else {
			throw new NoHandlerAssignedToActionException();
		}
		postServ();
	}
	
	final protected void setAction(String actionName, ActionHandler ah) {
		actionHandlers.put(actionName, ah);
	}
	
	protected void setDefaultAction(ActionHandler defaultActionHandler){
		defaultHandler = defaultActionHandler;
	}
	
	protected boolean preServ() throws IOException {
		return true;
	}
	protected void postServ() throws IOException {
		;
	}
}

@SuppressWarnings("serial")
final class NoHandlerAssignedToActionException extends RuntimeException {}; 