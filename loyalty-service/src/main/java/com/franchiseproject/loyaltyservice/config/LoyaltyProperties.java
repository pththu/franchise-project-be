package com.franchiseproject.loyaltyservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "loyalty")
public class LoyaltyProperties {
    private Rule rule = new Rule();
    private Tiers tiers = new Tiers();
    private Benefits benefits = new Benefits();

    @Data
    public static class Rule {
        private double amountPerPoint = 10000.0;
    }

    @Data
    public static class Tiers {
        private int silver = 500;
        private int gold = 1000;
        private int platinum = 2000;
        private int diamond = 3000;
    }

    @Data
    public static class Benefits {
        private List<String> bronze = new ArrayList<>();
        private List<String> silver = new ArrayList<>();
        private List<String> gold = new ArrayList<>();
        private List<String> platinum = new ArrayList<>();
        private List<String> diamond = new ArrayList<>();
    }
}