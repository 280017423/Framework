package com.qianjiang.framework.util;

/**
 * 业务异常
 * 
 * @author wang.xy
 * 
 */
public class MessageException extends Exception {
	private static final long serialVersionUID = 4521612743569217434L;
	public String Data;
	
	/**
	 * @param message 
	 */
	public MessageException(String message) {
		super(message);
	}
	
	/**
	 * @param message 
	 * @param data 
	 */
	public MessageException(String message, String data) {
		super(message);
		this.Data = data;
	}
}
