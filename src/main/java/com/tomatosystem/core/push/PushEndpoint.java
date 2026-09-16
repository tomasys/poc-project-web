package com.tomatosystem.core.push;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.cleopatra.json.JSONObject;


@ServerEndpoint(value = "/push")
public class PushEndpoint {
	
	private static List<Session> sessionList = new ArrayList<>();
	
	public PushEndpoint() {
	}
	
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) throws Exception {
		sessionList.add(session);
		System.out.println("onPushOpen");
	}
	
	@OnMessage
	public void onMessage(Session session, String message) throws Exception {
		System.out.println("onPushMessage");
		
		if(message.indexOf("{") != -1) {
			// share
			
			JSONObject req = new JSONObject(message);
			String sendMsg = req.getString("msg");
			
			int targetIndex =0;

			String strAppId = req.getString("id");
			if("shareData".equals(strAppId)) {
				targetIndex = 1;
			} 
			
			Session targetSession = sessionList.get(targetIndex);
			Basic basic = targetSession.getBasicRemote();
			basic.sendText(sendMsg);
		} else {
			// push
			
			int index = sessionList.indexOf(session);
			int targetIndex =0;
			if( index > 0 ) {
				targetIndex = index - 1;
			}
			Session targetSession = sessionList.get(targetIndex);
			Basic basic = targetSession.getBasicRemote();
			basic.sendText(message);
		}
	}
	
	@OnClose
	public void onClose(Session session, CloseReason reason) {
		sessionList.clear();
		System.out.println("onPushClose");
	}
	
	@OnError
	public void onError(Session session, Throwable t) throws Throwable {
		System.out.println("onPushError : ");
		t.printStackTrace();
	}


}
