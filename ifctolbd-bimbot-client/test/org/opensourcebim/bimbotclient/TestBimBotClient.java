package org.opensourcebim.bimbotclient;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.bimserver.bimbotclient.AccessToken;
import org.bimserver.bimbotclient.Application;
import org.bimserver.bimbotclient.Authorization;
import org.bimserver.bimbotclient.BimBotCall;
import org.bimserver.bimbotclient.BimBotClient;
import org.bimserver.bimbotclient.BimBotExecutionException;
import org.bimserver.bimbotclient.BimBotServer;
import org.bimserver.bimbotclient.BimBotServiceException;
import org.bimserver.bimbotclient.Service;
import org.bimserver.bimbotclient.ServiceProviderRegistry;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class TestBimBotClient {
	@Test
	public void test() {
		BimBotClient bimBotClient = new BimBotClient();
		
		ServiceProviderRegistry centralRegistry = bimBotClient.getCentralServiceProviderRegistry();
		BimBotServer experimentalServer;
		try {
//			experimentalServer = centralRegistry.findServiceProviderByName("Experimentalserver.com");
			experimentalServer = centralRegistry.findServiceProviderByName("Default localdev BIMserver");
			Service service = experimentalServer.findServiceByName("Simple Analyses Service");
			
			Application ourApplication = new Application("OAuth Java Client Test", "OAuth Java Client Test", "url", "redirecturl");
			experimentalServer.registerApplication(service.getRegisterUrl(), ourApplication);

			System.out.println("Navigate to " + service.constructAuthorizationUrl(ourApplication));
			System.out.print("Enter received redirect url: ");
			Scanner scanner = new Scanner(System.in);
			String redirectUri = scanner.nextLine();
			scanner.close();
			
			try {
				Authorization authorization = Authorization.parseUri(redirectUri);
				AccessToken accessToken = bimBotClient.acquireAccessToken(service, authorization, ourApplication);
				BimBotCall bimBotCall = new BimBotCall("IFC_STEP_2X3TC1", "UNSTRUCTURED_UTF8_TEXT_1_0", Resources.asByteSource(new URL("https://github.com/opensourceBIM/TestFiles/raw/master/TestData/data/AC11-Institute-Var-2-IFC.ifc")), accessToken);
				bimBotClient.execute(bimBotCall);
				System.out.println(new String(bimBotCall.getOutputData(), Charsets.UTF_8));
			} catch (BimBotExecutionException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		} catch (BimBotServiceException e1) {
			e1.printStackTrace();
		}
	}
}