package org.bimserver.bimbotclient;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bimserver.bimbotclient.beans.AccessToken;
import org.bimserver.bimbotclient.beans.Application;
import org.bimserver.bimbotclient.beans.Authorization;
import org.bimserver.bimbotclient.beans.Service;
import org.bimserver.bimbotclient.exeptions.BimBotExecutionException;
import org.bimserver.bimbotclient.exeptions.BimBotServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BimBotClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(BimBotClient.class);
	private CloseableHttpClient httpclient;
	private static final String CENTRAL_REPO_URL = "https://raw.githubusercontent.com/opensourceBIM/BIMserver-Repository/master/serviceproviders.json";
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public BimBotClient() {
		httpclient = HttpClients.createDefault();
	}
	
	public void execute(BimBotCall bimBotCall) throws BimBotExecutionException {
		bimBotCall.execute(httpclient);
	}
	
	public void close() {
		try {
			httpclient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected CloseableHttpClient getHttpclient() {
		return httpclient;
	}
	
	public BimBotServerConnection getServer(String name, String description, String serviceListPath) {
		return new BimBotServerConnection(this, name, description, serviceListPath);
	}

	public ServiceProviderRegistry getCentralServiceProviderRegistry() {
		return new ServiceProviderRegistry(this, CENTRAL_REPO_URL);
	}

	public AccessToken acquireAccessToken(Service service, Authorization authorization, Application application) throws BimBotServiceException, URISyntaxException {
		ObjectMapper objectMapper = new ObjectMapper();
		
		ObjectNode request = objectMapper.createObjectNode();
		request.put("client_id", application.getClientId());
		request.put("client_secret", application.getClientSecret());
		request.put("code", authorization.getCode());
		request.put("grant_type", "test");
		
		URIBuilder builder = new URIBuilder(service.getTokenUrl());
		builder.addParameter("grant_type", "authorization_code");
		builder.addParameter("code", authorization.getCode());
		builder.addParameter("redirect_uri", "why?");
		builder.addParameter("client_id", application.getClientId());
		builder.addParameter("client_secret", application.getClientSecret());
		HttpPost post = new HttpPost(builder.build().toString());
//		post.setEntity(new StringEntity(request.toString(), Charsets.UTF_8));
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		post.setHeader("Accept", "application/json");
		try {
			CloseableHttpResponse httpResponse = getHttpclient().execute(post);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				ObjectNode response = OBJECT_MAPPER.readValue(httpResponse.getEntity().getContent(), ObjectNode.class);
				LOGGER.info(response.toString());
				
				AccessToken accessToken = new AccessToken(response.get("access_token").asText(), response.get("resource_url").asText());
				return accessToken;
			} else {
				ObjectNode response = OBJECT_MAPPER.readValue(httpResponse.getEntity().getContent(), ObjectNode.class);
				LOGGER.error(response.toString());
				throw new BimBotServiceException("Unexpected HTTP status code: " + httpResponse.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			throw new BimBotServiceException(e);
		} catch (IOException e) {
			throw new BimBotServiceException(e);
		}
	}
}
