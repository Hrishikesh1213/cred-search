package com.cred.search.models.commons;

import com.cred.search.models.commons.enums.Tier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    private String seatId;
    private Tier tier;       // Uses your existing Enum
    private Boolean isBooked;
    private Double price;
    private String userId;
}