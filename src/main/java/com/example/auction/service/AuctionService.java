package com.example.auction.service;

import com.example.auction.dto.*;
import com.example.auction.enums.LotStatus;
import org.springframework.data.domain.Page;

public interface AuctionService {

    String getFirstBidder(int lotId);
    String getMostFrequentBidder(int lotId);
    FullLotDTO getFullLotById(int lotId);
    boolean startLot(int lotId);
    String createBid(int lotId, CreationBidDTO creationBidDTO);
    boolean stopLot(int lotId);
    LotDto createLot(CreationLotDTO lotRequest);
    Page<LotDto> findLotsByStatus(LotStatus status, int page);
    byte[] exportLotsToCSV();
}
