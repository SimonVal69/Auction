package com.example.auction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class LotDto implements Serializable {

    private int id;

    private String status;

    private String title;

    private String description;

    private int startPrice;

    private int bidPrice;

}
