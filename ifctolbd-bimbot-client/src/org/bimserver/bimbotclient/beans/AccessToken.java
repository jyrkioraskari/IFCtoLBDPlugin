package org.bimserver.bimbotclient.beans;

public class AccessToken {

	private String token;
	private String resourceUrl;

	public AccessToken(String token, String resourceUrl) {
		this.token = token;
		this.resourceUrl = resourceUrl;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getResourceUrl() {
		return resourceUrl;
	}
}