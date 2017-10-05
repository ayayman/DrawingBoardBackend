package com.example.drawingboard;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Optional;

@Component
public class WebSocketBoardHandler extends TextWebSocketHandler {

    private static Logger logger = Logger.getLogger(WebSocketBoardHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException, InterruptedException {
        Optional<Integer> roomNumber = getRoomNumberFromSession(session);
        if (roomNumber.isPresent()) {
            System.out.println("Connected with " + session.getRemoteAddress() + ", roomNumber: " + roomNumber);
        }
        else {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Got message: " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.debug("Connection with session " + session.getId() + " has been closed with status " + status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
    }

    private Optional<Integer> getRoomNumberFromSession(WebSocketSession session) {
        String roomNumberStr = (String) session.getAttributes().get("room");
        if (roomNumberStr == null) {
            return Optional.empty();
        }
        try {
            Integer roomNumber = Integer.parseInt(roomNumberStr);
            return Optional.of(roomNumber);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
