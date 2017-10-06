package com.example.drawingboard;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class RoomService {

    private final Map<WebSocketSession, Integer> sessionToRoomNumber = new HashMap<>();
    private final Map<Integer, Set<WebSocketSession>> roomNumberToSessions = new HashMap<>();
    private final Set<Integer> availableRoomNumbers = new HashSet<>();

    @PostConstruct
    private void init() {
        IntStream.range(1000, 10000).forEach(availableRoomNumbers::add);
    }

    public int createNewRoom() {
        int roomIndex = ThreadLocalRandom.current().nextInt(availableRoomNumbers.size());
        Integer roomNumber = availableRoomNumbers.stream().skip(roomIndex).findFirst().get();
        availableRoomNumbers.remove(roomNumber);
        roomNumberToSessions.put(roomNumber, new HashSet<>());
        return roomNumber;
    }

    public boolean isCreated(int roomNumber) {
        return roomNumberToSessions.get(roomNumber) != null;
    }

    public void deleteRoom(int roomNumber) {
        roomNumberToSessions.put(roomNumber, null);
        availableRoomNumbers.add(roomNumber);
    }

    public Set<WebSocketSession> getSessionsForRoom(int roomNumber) {
        if (!isCreated(roomNumber)) {
            throw new RuntimeException("Room number " + roomNumber + " hasn't been created");
        }
        return roomNumberToSessions.get(roomNumber);
    }

    public int getRoomForSession(WebSocketSession session) {
        Integer roomNumber = sessionToRoomNumber.get(session);
        if (roomNumber == null) {
            throw new RuntimeException("No corresponding roomNumber for session " + session);
        }
        return roomNumber;
    }

    public void registerSession(WebSocketSession session, int roomNumber) {
        if (!isCreated(roomNumber)) {
            throw new RuntimeException("Room number " + roomNumber + " hasn't been created");
        }
        sessionToRoomNumber.put(session, roomNumber);
        Set<WebSocketSession> sessions = roomNumberToSessions.get(roomNumber);
        sessions.add(session);
        roomNumberToSessions.put(roomNumber, sessions);
    }

    public void deregisterSession(WebSocketSession session) {
        Integer roomNumber = sessionToRoomNumber.get(session);
        if (roomNumber == null) {
            throw new RuntimeException("Such a session hasn't been registered");
        }
        sessionToRoomNumber.remove(session);
        Set<WebSocketSession> sessions = roomNumberToSessions.get(roomNumber);
        sessions.remove(session);
        roomNumberToSessions.put(roomNumber, sessions);
    }
}
