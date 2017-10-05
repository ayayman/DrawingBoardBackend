package com.example.drawingboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomController {

    private RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/api/rooms")
    public ResponseEntity<RoomInfo> createRoom() {
        int roomNumber = roomService.createNewRoom();
        RoomInfo roomInfo = new RoomInfo(roomNumber);
        return new ResponseEntity<RoomInfo>(roomInfo, HttpStatus.CREATED);
    }

    public static class RoomInfo {
        private Integer roomNumber;

        public Integer getRoomNumber() {
            return roomNumber;
        }

        public void setRoomNumber(Integer roomNumber) {
            this.roomNumber = roomNumber;
        }

        public RoomInfo(Integer roomNumber) {
            this.roomNumber = roomNumber;
        }
    }
}
