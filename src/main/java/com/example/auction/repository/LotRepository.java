package com.example.auction.repository;

import com.example.auction.enums.LotStatus;
import com.example.auction.model.Lot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LotRepository extends JpaRepository<Lot, Integer> {
    Page<Lot> findAllByStatus(LotStatus status, Pageable pageable);
}
