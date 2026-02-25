package com.franchiseproject.loyaltyservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "loyalty")
public class LoyaltyProperties {
    private Rule rule = new Rule();
    private Tiers tiers = new Tiers();

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
}