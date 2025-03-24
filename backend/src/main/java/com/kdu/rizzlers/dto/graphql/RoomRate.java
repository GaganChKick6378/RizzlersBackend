package com.kdu.rizzlers.dto.graphql;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRate {
    @JsonProperty("room_rate_id")
    private Integer roomRateId;
    
    @JsonProperty("basic_nightly_rate")
    private Double basicNightlyRate;
    
    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
} 