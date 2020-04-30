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

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import fi.iki.elonen.NanoHTTPD;


public class RWTHBimBotClient extends NanoHTTPD {
	final static private int PORT = 19090;
	private final BimBotClient bimBotClient = new BimBotClient();
	private Service service;
	private Application ourApplication;

	private final File ifcFile;

	public RWTHBimBotClient(String sIfcFile,String serviceURL,String serviceName) {
		super(RWTHBimBotClient.PORT);
		this.ifcFile=new File(sIfcFile);
		System.out.println("Running....");		
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		BimBotServerConnection localServer = new BimBotServerConnection(bimBotClient, "", "", serviceURL);
		try {
			this.service = localServer.findServiceByName(serviceName);

			this.ourApplication = new Application("RWTH OAuth Java Test Client", "RWTH OAuth Java Test Client", "url",
					"http://localhost:" + RWTHBimBotClient.PORT + "/redirect");
			localServer.registerApplication(service.getRegisterUrl(), ourApplication);

			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(new URI(service.constructAuthorizationUrl(ourApplication)));
			}

		} catch (BimBotServiceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// implement wait..
	}

	
	// Authentication response call-back 
	// This is called repeatedly
	@Override
	public Response serve(IHTTPSession session) {

		String msg = "<html><body><h1>Done</h1>\n";
		if (service == null)
			return newFixedLengthResponse(msg + "- No service available</body></html>\n");
		if (!session.getUri().equals("/redirect"))
			return newFixedLengthResponse(msg + "- Not serviced</body></html>\n");

		Map<String, String> parameters_map = session.getParms();
		Authorization authorization = new Authorization();
		for (String param : parameters_map.keySet()) {
			if (param.equals("code"))
				authorization.setCode(parameters_map.get(param));
			if (param.equals("address"))
				authorization.setAddress(parameters_map.get(param));
			if (param.equals("soid"))
				authorization.setSoid(parameters_map.get(param));
			if (param.equals("serviceaddress"))
				authorization.setServiceAddress(parameters_map.get(param));
		}

		try {
			AccessToken accessToken;
			try {
				accessToken = bimBotClient.acquireAccessToken(service, authorization, this.ourApplication);
				String response=call_bimbot_service(accessToken);
				return newFixedLengthResponse(msg+"\n"+response + "</body></html>\n");
			} catch (BimBotServiceException e) {
				e.printStackTrace();
			}

		} catch (BimBotExecutionException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return newFixedLengthResponse(msg + "</body></html>\n");
	}


	private String call_bimbot_service(AccessToken accessToken) throws BimBotExecutionException {
		if(!this.ifcFile.exists())
		{
			System.err.println("File not found.");
			return "";
		}
		ByteSource ifc_byteSource = Files.asByteSource(this.ifcFile);

		BimBotCall bimBotCall = new BimBotCall("IFC_STEP_2X3TC1", "UNSTRUCTURED_UTF8_TEXT_1_0",
				ifc_byteSource, accessToken);
		bimBotClient.execute(bimBotCall);
		System.out.println(new String(bimBotCall.getOutputData(), Charsets.UTF_8));
		return new String(bimBotCall.getOutputData(), Charsets.UTF_8);
	}

	public static void main(String[] args) {
		new RWTHBimBotClient("c:\\ifc\\231110AC-11-Smiley-West-04-07-2007.ifc","http://localhost:8080/servicelist","LinkedBuildingDataBIMBotService");
	}
}
