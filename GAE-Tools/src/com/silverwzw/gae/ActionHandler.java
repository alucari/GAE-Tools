package com.silverwzw.gae;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ActionHandler {
	public abstract void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
