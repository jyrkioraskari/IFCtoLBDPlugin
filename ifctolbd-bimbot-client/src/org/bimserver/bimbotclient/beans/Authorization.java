package org.bimserver.bimbotclient.beans;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.common.base.Charsets;

public class Authorization {

	private String code;
	private String address;
	private String soid;
	private String serviceAddress;

	public static Authorization parseUri(String redirectUri) throws URISyntaxException {
		List<NameValuePair> parse = URLEncodedUtils.parse(new URI(redirectUri), Charsets.UTF_8);
		Authorization authorization = new Authorization();
		for (NameValuePair nvp : parse) {
			if (nvp.getName().equals("code")) {
				authorization.code = nvp.getValue();
			} else if (nvp.getName().equals("address")) {
				authorization.address = nvp.getValue();
			} else if (nvp.getName().equals("soid")) {
				authorization.soid = nvp.getValue();
			} else if (nvp.getName().equals("serviceaddress")) {
				authorization.serviceAddress = nvp.getValue();
			}
		}
		return authorization;
	}

	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSoid() {
		return soid;
	}

	public void setSoid(String soid) {
		this.soid = soid;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
}