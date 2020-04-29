package org.bimserver.bimbotclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.bimserver.bimbotclient.exeptions.BimBotServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServiceProviderRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProviderRegistry.class);
	private final ObjectMapper objectMapper = new ObjectMapper();
	private BimBotClient bimBotClient;
	private String url;

	public ServiceProviderRegistry(BimBotClient bimBotClient, String url) {
		this.bimBotClient = bimBotClient;
		this.url = url;
	}
	
	public List<BimBotServerConnection> listServiceProviders() throws BimBotServiceException {
		List<BimBotServerConnection> serviceProviders = new ArrayList<>();
		LOGGER.info(url);
		HttpGet get = new HttpGet(url);
		try {
			CloseableHttpResponse httpResponse = bimBotClient.getHttpclient().execute(get);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				ObjectNode response = objectMapper.readValue(httpResponse.getEntity().getContent(), ObjectNode.class);
				ArrayNode activeProviders = (ArrayNode) response.get("active");
				for (JsonNode jsonNode : activeProviders) {
					serviceProviders.add(bimBotClient.getServer(jsonNode.get("name").asText(), jsonNode.get("description").asText(), jsonNode.get("listUrl").asText()));
				}
			} else {
				throw new BimBotServiceException("Unexpected HTTP status code: " + httpResponse.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			throw new BimBotServiceException(e);
		} catch (IOException e) {
			throw new BimBotServiceException(e);
		}
		
		return serviceProviders;
	}

	public BimBotServerConnection findServiceProviderByName(String name) throws BimBotServiceException {
		List<BimBotServerConnection> serviceProviders = listServiceProviders();
		for (BimBotServerConnection bimBotServer : serviceProviders) {
			if (bimBotServer.getName().equals(name)) {
				return bimBotServer;
			}
		}
		throw new BimBotServiceException("No service provider found with name \"" + name + "\"");
	}
}
