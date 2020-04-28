package org.bimserver.bimbotclient;

public class Application {

	private String name;
	private String redirectUrl;
	private String description;
	private String clientIcon = "";
	private String clientUrl;
	private String clientId;
	private String clientSecret;
	private long issuedAt;
	private long expiresIn;

	public Application(String name, String description, String clientUrl, String redirectUrl) {
		this.name = name;
		this.description = description;
		this.clientUrl = clientUrl;
		this.redirectUrl = redirectUrl;
	}

	public String getName() {
		return name;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public String getDescription() {
		return description;
	}

	public String getClientIcon() {
		return clientIcon;
	}

	public String getClientUrl() {
		return clientUrl;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public void setIssuedAt(long issuedAt) {
		this.issuedAt = issuedAt;
	}

	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public long getExpiresIn() {
		return expiresIn;
	}

	public long getIssuedAt() {
		return issuedAt;
	}
}
