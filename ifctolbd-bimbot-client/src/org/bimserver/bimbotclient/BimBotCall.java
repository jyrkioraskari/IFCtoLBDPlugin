package org.bimserver.bimbotclient;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

public class BimBotCall {
	private static final Logger LOGGER = LoggerFactory.getLogger(BimBotCall.class);
	private String inputType;
	private String outputType;
	private ByteSource inputData;
	private byte[] outputData;
	private String dataTitle;
	private String contentType;
	private AccessToken accessToken;

	public BimBotCall(String inputType, String outputType, ByteSource inputData, AccessToken accessToken) {
		this.inputType = inputType;
		this.outputType = outputType;
		this.inputData = inputData;
		this.accessToken = accessToken;
	}

	protected void execute(CloseableHttpClient httpclient) throws BimBotExecutionException {
		LOGGER.info(accessToken.getResourceUrl());
		HttpPost post = new HttpPost(accessToken.getResourceUrl());
		post.setHeader("Authorization", "Bearer " + accessToken.getToken());
		post.setHeader("Input-Type", inputType);
		try {
			post.setEntity(new InputStreamEntity(inputData.openBufferedStream()));
			CloseableHttpResponse httpResponse = httpclient.execute(post);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				outputData = ByteStreams.toByteArray(httpResponse.getEntity().getContent());
				outputType = httpResponse.getHeaders("Output-Type")[0].getValue();
				dataTitle = httpResponse.getHeaders("Data-Title")[0].getValue();
				contentType = httpResponse.getHeaders("Content-Type")[0].getValue();
			} else {
				throw new BimBotExecutionException("HTTP Status code: " + httpResponse.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			throw new BimBotExecutionException(e);
		} catch (IOException e) {
			throw new BimBotExecutionException(e);
		}
	}

	public byte[] getOutputData() {
		return outputData;
	}
	
	public String getOutputType() {
		return outputType;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getDataTitle() {
		return dataTitle;
	}
}