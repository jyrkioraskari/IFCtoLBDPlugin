package org.bimserver.bimbotclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;

public class BimBotServer {
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BimBotServer.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private String serviceListPath;
	private BimBotClient bimBotClient;
	private String name;
	private String description;

	public BimBotServer(BimBotClient bimBotClient, String name, String description, String serviceListPath) {
		this.bimBotClient = bimBotClient;
		this.name = name;
		this.description = description;
		this.serviceListPath = serviceListPath;
	}
	
	public String getServiceListUrl() {
		return serviceListPath;
	}
	
	public List<Service> listServices() throws BimBotServiceException {
		List<Service> services = new ArrayList<>();
		HttpGet get = new HttpGet(serviceListPath);
		try {
			CloseableHttpResponse httpResponse = bimBotClient.getHttpclient().execute(get);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				ObjectNode response = OBJECT_MAPPER.readValue(httpResponse.getEntity().getContent(), ObjectNode.class);
				LOGGER.info(response.toString());
				ArrayNode servicesArray = (ArrayNode) response.get("services");
				for (JsonNode serviceNode : servicesArray) {
					Service service = new Service(bimBotClient, (ObjectNode) serviceNode);
					services.add(service);
				}
			} else {
				throw new BimBotServiceException("Unexpected HTTP status code: " + httpResponse.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			throw new BimBotServiceException(e);
		} catch (IOException e) {
			throw new BimBotServiceException(e);
		}
		return services;
	}

	public Service findServiceByName(String name) throws BimBotServiceException {
		List<Service> services = listServices();
		for (Service service : services) {
			if (service.getName().equals(name)) {
				return service;
			}
		}
		throw new BimBotServiceException("No service found called \"" + name + "\" on " + serviceListPath);
	}

	public void registerApplication(String registrationUrl, Application application) throws BimBotServiceException {
		ObjectMapper objectMapper = new ObjectMapper();
		LOGGER.info(registrationUrl);
		
		ObjectNode request = objectMapper.createObjectNode();
		request.put("redirect_url", application.getRedirectUrl());
		request.put("client_name", application.getName());
		request.put("client_description", application.getDescription());
		request.put("client_icon", application.getClientIcon());
		request.put("client_url", application.getClientUrl());
		request.put("type", "pull");
		
		HttpPost post = new HttpPost(registrationUrl);
		post.setEntity(new StringEntity(request.toString(), Charsets.UTF_8));
		post.setHeader("Content-Type", "application/json");
		try {
			CloseableHttpResponse httpResponse = bimBotClient.getHttpclient().execute(post);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				ObjectNode response = OBJECT_MAPPER.readValue(httpResponse.getEntity().getContent(), ObjectNode.class);
				LOGGER.info(response.toString());
				
				application.setClientId(response.get("client_id").asText());
				application.setClientSecret(response.get("client_secret").asText());
				application.setIssuedAt(response.get("issued_at").asLong());
				application.setExpiresIn(response.get("expires_in").asLong());
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
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
}