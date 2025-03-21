package com.kdu.rizzlers.dto.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {
    @JsonProperty("room_type_id")
    private Integer roomTypeId;
    
    @JsonProperty("room_type_name")
    private String roomTypeName;
    
    @JsonProperty("max_capacity")
    private Integer maxCapacity;
    
    @JsonProperty("room_rates")
    private List<RoomRateRoomTypeMapping> roomRates;
} 