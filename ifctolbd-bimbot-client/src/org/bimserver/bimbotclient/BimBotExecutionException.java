package org.bimserver.bimbotclient;

public class BimBotExecutionException extends Exception {

	private static final long serialVersionUID = -1679707962395405018L;

	public BimBotExecutionException(Exception e) {
		super(e);
	}

	public BimBotExecutionException(String message) {
		super(message);
	}
}
