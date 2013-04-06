package com.silverwzw.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ActionHandler {
	public void serv(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}