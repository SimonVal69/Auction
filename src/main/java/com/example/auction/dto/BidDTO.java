package com.example.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class BidDTO implements Serializable {

    private String bidderName;

    private Timestamp bidTime;

    @Override
    public String toString() {
        return "{\n" +
                "  \"bidderName\": \"" + bidderName + "\",\n" +
                "  \"bidDate\": \"" + bidTime + "\"\n" +
                "}";
    }

}
