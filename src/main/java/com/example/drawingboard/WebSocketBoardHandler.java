package com.example.drawingboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.example.drawingboard.BoardLineMessage.Type.*;

@Component
public class WebSocketBoardHandler extends TextWebSocketHandler {

    private static Logger logger = Logger.getLogger(WebSocketBoardHandler.class);

    private RoomService roomService;
    private BoardService boardService;
    private MappingJackson2HttpMessageConverter springMvcJacksonConverter;

    @Autowired
    public WebSocketBoardHandler(RoomService roomService, BoardService boardService, MappingJackson2HttpMessageConverter springMvcJacksonConverter) {
        this.roomService = roomService;
        this.boardService = boardService;
        this.springMvcJacksonConverter = springMvcJacksonConverter;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException, InterruptedException {
        Optional<Integer> roomNumberOpt = getRoomNumberFromSession(session);
        if (roomNumberOpt.isPresent()) {
            Integer roomNumber = roomNumberOpt.get();
            ObjectMapper objectMapper = getObjectMapper();

            roomService.registerSession(session, roomNumber);
            List<BoardLineMessage> boardMessages = boardService.getAllLines(roomNumber);
            for (BoardLineMessage message : boardMessages) {
                String serialized = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(serialized));
            }

            broadcastInfoToRoomMembers(roomNumber);
        }
        else {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        int roomNumber = roomService.getRoomForSession(session);

        ObjectMapper objectMapper = getObjectMapper();
        String messageBody = message.getPayload();
        BoardLineMessage boardMessage = objectMapper.readValue(messageBody, BoardLineMessage.class);
        if (boardMessage.getType() == ADD) {
            boardService.addLine(roomNumber, boardMessage);
        }
        else if (boardMessage.getType() == CLEAR) {
            boardService.clearAllLines(roomNumber);
        }
        else if (boardMessage.getType() == REDRAW) {
            List<BoardLineMessage> boardMessages = boardService.getAllLines(roomNumber);
            for (BoardLineMessage aMessage : boardMessages) {
                String serialized = objectMapper.writeValueAsString(aMessage);
                session.sendMessage(new TextMessage(serialized));
            }
        }
        Set<WebSocketSession> sessionsInRoom = roomService.getSessionsForRoom(roomNumber);
        for (WebSocketSession sessionInRoom : sessionsInRoom) {
            if (!sessionInRoom.equals(session)) {
                sessionInRoom.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        int roomNumber = roomService.getRoomForSession(session);
        roomService.deregisterSession(session);
        broadcastInfoToRoomMembers(roomNumber);
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

    private ObjectMapper getObjectMapper() {
        return springMvcJacksonConverter.getObjectMapper();
    }

    private void broadcastInfoToRoomMembers(int roomNumber) throws IOException {
        ObjectMapper objectMapper = getObjectMapper();
        Set<WebSocketSession> allSessionsInRoom = roomService.getSessionsForRoom(roomNumber);

        BoardLineMessage infoMessage = new BoardLineMessage();
        infoMessage.setType(INFO);
        infoMessage.setPeopleCount(allSessionsInRoom.size());

        for (WebSocketSession sessionInRoom : allSessionsInRoom) {
            String serialized = objectMapper.writeValueAsString(infoMessage);
            sessionInRoom.sendMessage(new TextMessage(serialized));
        }
    }
}
