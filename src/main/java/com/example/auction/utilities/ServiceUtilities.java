package com.example.auction.utilities;

import com.example.auction.dto.BidDTO;
import com.example.auction.model.Bid;
import com.example.auction.model.Lot;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class ServiceUtilities {

    public int calculateCurrentPrice(Lot lot) {
        int numberOfBids = lot.getBidsById().size();
        return numberOfBids * lot.getBidPrice() + lot.getStartPrice();
    }

    public BidDTO getLastBidDTO(Lot lot) {
        List<Bid> bids = lot.getBidsById();
        if (bids.isEmpty()) {
            return new BidDTO();
        }
        Bid lastBid = bids.get(bids.size() - 1);
        BidDTO lastBidDTO = new BidDTO();
        lastBidDTO.setBidderName(lastBid.getBidderName());
        lastBidDTO.setBidTime(lastBid.getBidTime());
        return lastBidDTO;
    }
}
