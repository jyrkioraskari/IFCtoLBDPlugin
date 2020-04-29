package org.bimserver.bimbotclient.exeptions;

public class BimBotServiceException extends Exception {

	private static final long serialVersionUID = 4160292495485609592L;

	public BimBotServiceException(String string) {
		super(string);
	}

	public BimBotServiceException(Exception e) {
		super(e);
	}
}
