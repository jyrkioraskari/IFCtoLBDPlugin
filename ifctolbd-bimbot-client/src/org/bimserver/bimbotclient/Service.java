package org.bimserver.bimbotclient;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.utils.URIBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Service {

	private BimBotClient bimBotClient;
	private JsonNode serviceNode;
	private String id;
	private String name;
	private String description;
	private String provider;
	private String providerIcon;
	private Set<String> inputs = new HashSet<>();
	private Set<String> outputs = new HashSet<>();
	private String resourceUrl;
	private String authorizationUrl;
	private String registerUrl;
	private String tokenUrl;

	public Service(BimBotClient bimBotClient, ObjectNode serviceNode) {
		this.bimBotClient = bimBotClient;
		this.serviceNode = serviceNode;
		this.id = serviceNode.get("id").asText();
		this.name = serviceNode.get("name").asText();
		this.description = serviceNode.get("description").asText();
		this.provider = serviceNode.get("provider").asText();
		this.providerIcon = serviceNode.get("providerIcon").asText();
		ArrayNode inputs = (ArrayNode) serviceNode.get("inputs");
		ArrayNode outputs = (ArrayNode) serviceNode.get("outputs");
		for (JsonNode inputNode : inputs) {
			this.inputs.add(inputNode.asText());
		}
		for (JsonNode outputNode : outputs) {
			this.outputs.add(outputNode.asText());
		}
		ObjectNode oauthNode = (ObjectNode)serviceNode.get("oauth");
		this.authorizationUrl = oauthNode.get("authorizationUrl").asText();
		this.registerUrl = oauthNode.get("registerUrl").asText();
		this.tokenUrl = oauthNode.get("tokenUrl").asText();
		this.resourceUrl = serviceNode.get("resourceUrl").asText();
	}

	public String getRegisterUrl() {
		return registerUrl;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAuthorizationUrl() {
		return authorizationUrl;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getId() {
		return id;
	}
	
	public Set<String> getInputs() {
		return inputs;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public Set<String> getOutputs() {
		return outputs;
	}
	
	public String getResourceUrl() {
		return resourceUrl;
	}
	
	public JsonNode getServiceNode() {
		return serviceNode;
	}
	
	public String getTokenUrl() {
		return tokenUrl;
	}
	
	public String getProviderIcon() {
		return providerIcon;
	}

	public String constructAuthorizationUrl(Application application) {
		try {
			ObjectNode state = BimBotClient.OBJECT_MAPPER.createObjectNode();
			state.put("_serviceName", getName());
			
			URIBuilder uriBuilder = new URIBuilder(getAuthorizationUrl());
			uriBuilder.addParameter("redirect_uri", application.getRedirectUrl());
			uriBuilder.addParameter("response_type", "code");
			uriBuilder.addParameter("client_id", application.getClientId());
			uriBuilder.addParameter("auth_type", "service");
			uriBuilder.addParameter("state", state.toString());
			
			return uriBuilder.build().toString();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		return null;
	}
}