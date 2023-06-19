package com.example.auction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CreateBidDTO implements Serializable {
    private String bidderName;
}
