package com.kesdip.player.components;

/**
 * Used to signal some abnormal condition in the Component interface.
 * 
 * @author Pafsanias Ftakas
 */
public class ComponentException extends Exception {
	private static final long serialVersionUID = 1L;

	public ComponentException(String msg) {
		super(msg);
	}
	
	public ComponentException(String msg, Throwable t) {
		super(msg, t);
	}
}
