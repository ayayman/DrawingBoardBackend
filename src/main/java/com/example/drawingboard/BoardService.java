package com.example.drawingboard;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BoardService {

    private final Map<Integer, List<BoardLineMessage>> roomNumberToLines = new HashMap<>();

    public void addLine(int roomNumber, BoardLineMessage line) {
        List<BoardLineMessage> lines = roomNumberToLines.getOrDefault(roomNumber, new ArrayList<>());
        lines.add(line);
        roomNumberToLines.put(roomNumber, lines);
    }

    public List<BoardLineMessage> getAllLines(int roomNumber) {
        return roomNumberToLines.getOrDefault(roomNumber, new ArrayList<>());
    }

    public void clearAllLines(int roomNumber) {
        roomNumberToLines.put(roomNumber, new ArrayList<>());
    }
}
