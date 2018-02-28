package eu.driver.admin.service.ws;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class WSController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private static WSController aMe;
	private WebSocketSession session = null;
	
	private WSController() {
		log.info("WSController");
	}
	
	public static synchronized WSController getInstance() {
		if (WSController.aMe == null) {
			WSController.aMe = new WSController();
		}
		return WSController.aMe;
	}
	
	public void setWSSession(WebSocketSession session) {
		log.info("setWSSession");
		this.session = session;
		log.info("setWSSession");
	}
	
	public void sendMessage(String msg) {
		log.info("--> sendMessage");
		log.debug(msg);
		TextMessage responseMsg = new TextMessage(msg);
		try {
			if (session != null && session.isOpen()) {
				session.sendMessage(responseMsg);
			}
		} catch (IOException e) {
			log.error("Error sending the notification!");
		}
		log.info("sendMessage -->");
	}

}
