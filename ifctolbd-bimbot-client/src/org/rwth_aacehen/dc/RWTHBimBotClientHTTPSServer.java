package org.rwth_aacehen.dc;

import java.io.IOException;
import java.util.Map;

import org.bimserver.bimbotclient.beans.Authorization;
import org.bimserver.bimbotclient.beans.Service;
import org.rwth_aacehen.dc.messages.StopRequestEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import fi.iki.elonen.NanoHTTPD;

public class RWTHBimBotClientHTTPSServer extends NanoHTTPD implements Runnable {
	private static final EventBus eventBus = EventBusService.getEventBus();
	final static public int PORT = 19090;
	private final Service service;
	private boolean first=true;

	public RWTHBimBotClientHTTPSServer(Service service) {
		super(RWTHBimBotClientHTTPSServer.PORT);
		this.service = service;
		new Thread(this).start();

	}

	@Override
	public void run() {
		eventBus.register(this);
		System.out.println("Start server");

		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	// Authentication response call-back
	// This is called repeatedly
	@Override
	public Response serve(IHTTPSession session) {
		System.out.println("WWW reply call");
		String msg = "<html><body><h1>OAuth 2.0 authentication is fine.</h1>\n";
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
		if(first)			
		   eventBus.post(authorization);
		first=false;
		System.out.println("Web response");

		return newFixedLengthResponse(msg + "</body></html>\n");
	}

	@Subscribe
	public void handleStopRequestEvent(StopRequestEvent event) {
		System.out.println("request to stop www service");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.closeAllConnections();
		this.stop();
	}

}
