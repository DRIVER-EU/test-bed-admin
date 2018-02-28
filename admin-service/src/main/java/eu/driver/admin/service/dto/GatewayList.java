package eu.driver.admin.service.dto;

import java.util.ArrayList;
import java.util.List;

import eu.driver.admin.service.dto.gateway.Gateway;

public class GatewayList {
	
	private List<Gateway> gateways = new ArrayList<Gateway>();
	
	public GatewayList() {
		
	}

	public List<Gateway> getGateways() {
		return gateways;
	}

	public void setGateways(List<Gateway> gateways) {
		this.gateways = gateways;
	}

}
