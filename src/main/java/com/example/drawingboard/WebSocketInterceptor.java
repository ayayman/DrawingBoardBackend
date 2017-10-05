package com.example.drawingboard;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    private static final Pattern roomPattern = Pattern.compile(".*room=([0-9]+)$");

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        String queryString = request.getURI().getQuery();
        if (queryString != null) {
            Optional<String> roomOptional = extractRoomFromUri(queryString);
            if (roomOptional.isPresent()) {
                String webSocketToken = roomOptional.get();
                attributes.put("room", roomOptional.get());
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
    }

    private Optional<String> extractRoomFromUri(String queryString) {
        Matcher matcher = roomPattern.matcher(queryString);
        if (matcher.find() && matcher.groupCount() == 1) {
            return Optional.of(matcher.group(1));
        }
        else {
            return Optional.empty();
        }
    }
}
