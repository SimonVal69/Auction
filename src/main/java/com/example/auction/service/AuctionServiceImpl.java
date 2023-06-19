package com.example.auction.service;

import com.example.auction.dto.*;
import com.example.auction.enums.LotStatus;
import com.example.auction.model.Bid;
import com.example.auction.model.Lot;
import com.example.auction.repository.BidRepository;
import com.example.auction.repository.LotRepository;
import com.example.auction.utilities.ServiceUtilities;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuctionServiceImpl implements AuctionService {

    private final LotRepository lotRepository;
    private final BidRepository bidRepository;
    private final ModelMapper modelMapper;
    private final ServiceUtilities serviceUtilities;
    private static final Logger logger = LoggerFactory.getLogger(AuctionServiceImpl.class);

    public AuctionServiceImpl(LotRepository lotRepository, BidRepository bidRepository, ModelMapper modelMapper, ServiceUtilities serviceUtilities) {
        this.lotRepository = lotRepository;
        this.bidRepository = bidRepository;
        this.modelMapper = modelMapper;
        this.serviceUtilities = serviceUtilities;
    }

    @Override
    public String getFirstBidder(int lotId) {
        Lot lot = lotRepository.findById(lotId).orElse(null);
        if (lot == null) {
            return "Лот не найден";
        }
        if (lot.getStatus() == LotStatus.CREATED) {
            return "Лот в неверном статусе";
        }
        Optional<Bid> firstBidOptional = lot.getBidsById().stream().findFirst();
        if (firstBidOptional.isEmpty()) {
            return "Заявок по этому лоту нет";
        }
            Bid firstBid = firstBidOptional.get();
            BidDTO firstBidder = new BidDTO();
            firstBidder.setBidderName(firstBid.getBidderName());
            firstBidder.setBidTime(firstBid.getBidTime());
            return firstBidder.toString();
    }

    @Override
    public String getMostFrequentBidder(int lotId) {
        Lot lot = lotRepository.findById(lotId).orElse(null);
        if (lot == null) {
            return "Лот не найден";
        }
        List<Bid> bids = lot.getBidsById();
        if (bids.isEmpty()) {
            return "Заявок по этому лоту нет";
        }
        Map<String, Long> bidderCounts = bids.stream()
                .collect(Collectors.groupingBy(Bid::getBidderName, Collectors.counting()));
        long maxBidCount = bidderCounts.values().stream()
                .max(Long::compare)
                .orElse(0L);
        List<String> mostFrequentBidders = bidderCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == maxBidCount)
                .map(Map.Entry::getKey)
                .toList();
        if (mostFrequentBidders.size() != 1) {
            return "Не удалось определить наиболее активного участника";
        }
        String mostFrequentBidderName = mostFrequentBidders.get(0);
        Optional<Bid> mostFrequentBidderLastBid = bids.stream()
                .filter(bid -> bid.getBidderName().equals(mostFrequentBidderName))
                .max(Comparator.comparing(Bid::getBidTime));
        BidDTO mostFrequentBidder = new BidDTO();
        mostFrequentBidder.setBidderName(mostFrequentBidderName);
        mostFrequentBidder.setBidTime(mostFrequentBidderLastBid.get().getBidTime());
        return mostFrequentBidder.toString();
    }

    @Override
    public FullLotDTO getFullLotById(int lotId) {
        Lot lot = lotRepository.findById(lotId).orElse(null);
        FullLotDTO fullLotDTO = new FullLotDTO();
        if (lot != null) {
            modelMapper.map(lot, fullLotDTO);
            fullLotDTO.setCurrentPrice(lot.getStatus() != LotStatus.CREATED ? serviceUtilities.calculateCurrentPrice(lot) : lot.getStartPrice());
            fullLotDTO.setLastBid(serviceUtilities.getLastBidDTO(lot));
        }
        return fullLotDTO;
    }

    @Override
    public boolean startLot(int lotId) {
        try {
            Lot lot = lotRepository.findById(lotId).orElseThrow();
            if (lot.getStatus() != LotStatus.STARTED) {
                lot.setStatus(LotStatus.STARTED);
                lotRepository.save(lot);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String createBid(int lotId, CreateBidDTO createBidDTO) {
        Lot lot = lotRepository.findById(lotId).orElse(null);
        if (lot == null) {
            return "Лот не найден";
        }
        if (lot.getStatus() != LotStatus.STARTED) {
            return "Лот в неверном статусе";
        }
        Bid bid = new Bid();
        bid.setBidderName(createBidDTO.getBidderName());
        bid.setBidTime(Timestamp.valueOf(LocalDateTime.now().plusHours(4)));
        bid.setLotByLotId(lot);
        bidRepository.save(bid);
        return "Ставка создана";
    }

    @Override
    public boolean stopLot(int lotId) {
        try {
            Lot lot = lotRepository.findById(lotId).orElseThrow();
            if (lot.getStatus() != LotStatus.STOPPED) {
                lot.setStatus(LotStatus.STOPPED);
                lotRepository.save(lot);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public LotDto createLot(CreateLotDTO createLotDTO) {
        Lot lot =  modelMapper.map(createLotDTO, Lot.class);
        lot.setStatus(LotStatus.CREATED);
        lotRepository.save(lot);
        return modelMapper.map(lot, LotDto.class);
    }

    @Override
    public Page<LotDto> findLotsByStatus(LotStatus status, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Lot> lotPage = lotRepository.findAllByStatus(status, pageable);
        return lotPage.map(lot -> modelMapper.map(lot, LotDto.class));
    }

    @Override
    public byte[] exportLotsToCSV() {
        List<Lot> lots = lotRepository.findAll();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.POSTGRESQL_CSV)) {
            csvPrinter.printRecord("id", "title", "status", "lastBidder", "currentPrice");
            lots.stream()
                    .map(lot -> Arrays.asList(
                            lot.getId(),
                            lot.getTitle(),
                            lot.getStatus(),
                            lot.getBidsById().isEmpty() ? "нет ставок" : lot.getBidsById().get(lot.getBidsById().size() - 1).getBidderName(),
                            serviceUtilities.calculateCurrentPrice(lot)
                    ))
                    .forEach(record -> {
                        try {
                            csvPrinter.printRecord(record);
                        } catch (IOException e) {
                            e.printStackTrace();
                            logger.error("Ошибка I/O в csvPrinter");
                        }
                    });
            csvPrinter.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Ошибка I/O в потоке");
            return new byte[0];
        }
    }
}
