package com.example.auction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CreationBidDTO implements Serializable {
    private String bidderName;
}
