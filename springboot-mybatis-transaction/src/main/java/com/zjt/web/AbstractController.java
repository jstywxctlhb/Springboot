package com.zjt.web;

import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractController {
	
	@Autowired
	private HttpServletRequest request;
	
	protected String getContextPath(){

		return request.getContextPath();
	}
}