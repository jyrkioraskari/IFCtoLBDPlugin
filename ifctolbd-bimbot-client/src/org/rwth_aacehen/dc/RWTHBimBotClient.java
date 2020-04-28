package org.rwth_aacehen.dc;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.bimserver.bimbotclient.AccessToken;
import org.bimserver.bimbotclient.Application;
import org.bimserver.bimbotclient.Authorization;
import org.bimserver.bimbotclient.BimBotCall;
import org.bimserver.bimbotclient.BimBotClient;
import org.bimserver.bimbotclient.BimBotExecutionException;
import org.bimserver.bimbotclient.BimBotServer;
import org.bimserver.bimbotclient.BimBotServiceException;
import org.bimserver.bimbotclient.Service;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import fi.iki.elonen.NanoHTTPD;

public class RWTHBimBotClient  extends NanoHTTPD{
	final static private int PORT=19090;
	private BimBotClient bimBotClient = new BimBotClient();
	private Service service;
	private Application ourApplication;
	
	private ByteSource ifc_byteSource;

	public RWTHBimBotClient() {
		super(RWTHBimBotClient.PORT);
		System.out.println("Running....");
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ifc_byteSource = Files.asByteSource(new File("c:\\ifc\\231110AC-11-Smiley-West-04-07-2007.ifc"));
		
		BimBotServer localServer=new BimBotServer(bimBotClient, "", "", "http://localhost:8080/servicelist");
		try {
			this.service = localServer.findServiceByName("LinkedBuildingDataBIMBotService");
			
			this.ourApplication = new Application("RWTH OAuth Java Test Client", "RWTH OAuth Java Test Client", "url", "http://localhost:"+RWTHBimBotClient.PORT+"/redirect");
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
		
	}
	
	 @Override
     public Response serve(IHTTPSession session) {
		 
				 
         String msg = "<html><body><h1>Done</h1>\n";
		 if(service==null)
			 return newFixedLengthResponse(msg + "- No service available</body></html>\n");
		 if(!session.getUri().equals("/redirect"))
			 return newFixedLengthResponse(msg + "- Not serviced</body></html>\n");
				 
				 
         Map<String, String> parameters_map = session.getParms();
         Authorization authorization = new Authorization();
         for(String param: parameters_map.keySet())
         {
        	 if (param.equals("code")) {
 				authorization.setCode(parameters_map.get(param));
 			}
        	 if (param.equals("address")) {
 				authorization.setAddress(parameters_map.get(param));
 			}
        	 if (param.equals("soid")) {
 				authorization.setSoid(parameters_map.get(param));
 			} 
        	if(param.equals("serviceaddress")) {
 				authorization.setServiceAddress(parameters_map.get(param));
 			}
         }
         
         try {
				AccessToken accessToken;
				try {
					accessToken = bimBotClient.acquireAccessToken(service, authorization, this.ourApplication);
					BimBotCall bimBotCall = new BimBotCall("IFC_STEP_2X3TC1", "UNSTRUCTURED_UTF8_TEXT_1_0", this.ifc_byteSource, accessToken);
					bimBotClient.execute(bimBotCall);
					System.out.println(new String(bimBotCall.getOutputData(), Charsets.UTF_8));
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
	
	public static void main(String[] args) {
		new RWTHBimBotClient();
	}
}
