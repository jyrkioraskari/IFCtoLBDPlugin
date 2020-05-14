package org.rwth_aacehen.dc;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.bimserver.bimbotclient.BimBotCall;
import org.bimserver.bimbotclient.BimBotClient;
import org.bimserver.bimbotclient.BimBotServerConnection;
import org.bimserver.bimbotclient.beans.AccessToken;
import org.bimserver.bimbotclient.beans.Application;
import org.bimserver.bimbotclient.beans.Authorization;
import org.bimserver.bimbotclient.beans.Service;
import org.bimserver.bimbotclient.exeptions.BimBotExecutionException;
import org.bimserver.bimbotclient.exeptions.BimBotServiceException;
import org.rwth_aacehen.dc.messages.StopRequestEvent;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import fi.iki.elonen.NanoHTTPD;

public class RWTHBimBotClient {
	private static final EventBus eventBus = EventBusService.getEventBus();
	private final BimBotClient bimBotClient = new BimBotClient();
	private Service service;
	private Application ourApplication;

	private AccessToken accessToken = null;
	private Integer accessTokenSemafore = new Integer(0);
	private final File ifcFile;

	public RWTHBimBotClient(String sIfcFile, String serviceURL, String serviceName) {

		this.ifcFile = new File(sIfcFile);
		System.out.println("Running....");

		BimBotServerConnection localServer = new BimBotServerConnection(bimBotClient, "", "", serviceURL);
		try {
			this.service = localServer.findServiceByName(serviceName);

			this.ourApplication = new Application("RWTH OAuth Java Test Client", "RWTH OAuth Java Test Client", "url",
					"http://localhost:" + RWTHBimBotClientHTTPSServer.PORT + "/redirect");
			localServer.registerApplication(service.getRegisterUrl(), ourApplication);

			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(new URI(service.constructAuthorizationUrl(ourApplication)));
			}

		} catch (BimBotServiceException e) {
			System.out.println("BIMServer is not responding: "+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		this.eventBus.register(this);
		if (accessToken == null) {
			RWTHBimBotClientHTTPSServer webServer = new RWTHBimBotClientHTTPSServer(service);
			try {
				synchronized (accessTokenSemafore) {
					accessTokenSemafore.wait();
				}
				System.out.println("wait done");
				this.eventBus.post(new StopRequestEvent());
			} catch (InterruptedException e) {
				;
			}
		}
		try {
			System.out.println("call service");
			String response = call_bimbot_service(accessToken);
		} catch (BimBotExecutionException e) {
			e.printStackTrace();
		}
	}

	private String call_bimbot_service(AccessToken accessToken) throws BimBotExecutionException {
		if (!this.ifcFile.exists()) {
			System.err.println("File not found.");
			return "";
		}
		ByteSource ifc_byteSource = Files.asByteSource(this.ifcFile);

		BimBotCall bimBotCall = new BimBotCall("IFC_STEP_2X3TC1", "UNSTRUCTURED_UTF8_TEXT_1_0", ifc_byteSource,
				accessToken);
		bimBotClient.execute(bimBotCall);
		System.out.println(new String(bimBotCall.getOutputData(), Charsets.UTF_8));
		return new String(bimBotCall.getOutputData(), Charsets.UTF_8);
	}

	@Subscribe
	public void handleAuthorizationEvent(Authorization authorization) {
		System.out.println("Got authorization");

		try {

			try {
				accessToken = bimBotClient.acquireAccessToken(service, authorization, this.ourApplication);
				synchronized (accessTokenSemafore) {
					accessTokenSemafore.notify();
				}

			} catch (BimBotServiceException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new RWTHBimBotClient("c:\\test\\Duplex_A_20110505.ifc", "http://localhost:8080/servicelist",
				"LinkedBuildingDataBIMBotService");
	}
}
