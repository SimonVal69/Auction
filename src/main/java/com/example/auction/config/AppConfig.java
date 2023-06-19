package com.example.auction.config;

import com.example.auction.utilities.ServiceUtilities;
import org.modelmapper.ModelMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ServiceUtilities serviceUtilities() {
        return new ServiceUtilities();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("firstBidder", "mostFrequentBidder",
                "fullLot", "lotsByStatus", "createBid", "exportLotsToCSV");
    }
}

