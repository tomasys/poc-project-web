package com.tomatosystem.core.push;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import com.tomatosystem.core.util.ClientWebUtil;

public class HttpHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest =
                    ((ServletServerHttpRequest) request).getServletRequest();
            
            String ip = ClientWebUtil.getAccessIp(servletRequest);
            String os = ClientWebUtil.getOsTypeInfo(servletRequest);
            String brower = ClientWebUtil.getBrowserInfo(servletRequest);
            HttpSession session = servletRequest.getSession(false);
            attributes.put("ip", ip);
            attributes.put("os", os);
            attributes.put("brower", brower);
            attributes.put("userType", session.getAttribute("userType"));
            attributes.put("userNm", session.getAttribute("userNm"));
            LocalDateTime currentDateTime = LocalDateTime.now();
    	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    	    String toDate = currentDateTime.format(formatter);
            attributes.put("acsDate", toDate);
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Do nothing
    }
}