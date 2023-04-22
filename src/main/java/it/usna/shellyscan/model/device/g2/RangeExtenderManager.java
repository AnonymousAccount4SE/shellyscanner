package it.usna.shellyscan.model.device.g2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public class RangeExtenderManager {
//	private final AbstractG2Device d;
//	private int exConnected;
	private List<Integer> ports = new ArrayList<>();

	public RangeExtenderManager(AbstractG2Device d) throws IOException {
//		this.d = d;
//		JsonNode wifi = d.getJSON("/rpc/Wifi.GetStatus");
//		exConnected = wifi.path("ap_client_count").asInt();
		
		JsonNode clients = d.getJSON("/rpc/WiFi.ListAPClients");
		if(clients.isNull() == false) {
			clients.get("ap_clients").forEach(c -> ports.add(c.get("mport").asInt()));
//			exConnected = ports.size();
		} /*else {
			exConnected = 0;
		}*/
	}
	
	public int numExConnected() {
		return ports.size();
	}
	
	public List<Integer> getPorts() {
		return ports;
	}
}
