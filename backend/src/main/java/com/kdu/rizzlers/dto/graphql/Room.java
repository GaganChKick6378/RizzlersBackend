package com.kdu.rizzlers.dto.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @JsonProperty("room_id")
    private Integer roomId;
    
    @JsonProperty("room_number")
    private String roomNumber;
    
    @JsonProperty("room_type")
    private RoomType roomType;
} 