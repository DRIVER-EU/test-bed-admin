package eu.driver.admin.service.ws;

import java.util.Timer;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import eu.driver.admin.service.ws.mapper.StringJSONMapper;
import eu.driver.admin.service.ws.object.WSHeartbeatRequest;

@Component
public class WebSocketServer extends TextWebSocketHandler {
	
	private Logger log = Logger.getLogger(this.getClass());
	private Timer timer = null;
	private StringJSONMapper mapper = new StringJSONMapper();
	
	public WebSocketServer() {

	}
	 
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    	log.info("The WebSocket has been closed!");
    	this.timer.cancel();
    	this.timer = null;
    }
 
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    	log.info("The WebSocket has been opened!");
    	WSController.getInstance().setWSSession(session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
    	log.info("Message received: " + textMessage.getPayload());
    	
    	String message = textMessage.getPayload();
    	if (message.indexOf("eu.driver.adaptor.ws.request.heartbeat") != -1) {
    		WSHeartbeatRequest hbRequest = mapper.stringToHBRequestMessage(textMessage.getPayload());
    		
    		TextMessage responseMsg = new TextMessage(mapper.objectToJSONString(hbRequest.createResponse()));
    		session.sendMessage(responseMsg);
    	}
    }
}
