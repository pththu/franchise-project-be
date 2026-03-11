package franchiseproject.promotion_service.service.impl;

import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl {

    private final PromotionRepository promotionRepository;

    // View Promotions
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // View Promotion Details
    public Promotion getPromotionById(UUID id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
    }

    // Create Promotion
    public Promotion createPromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    // Update Promotion
    public Promotion updatePromotion(UUID id, Promotion promotion) {

        Promotion existing = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        existing.setName(promotion.getName());
        existing.setDescription(promotion.getDescription());
        existing.setDiscountType(promotion.getDiscountType());
        existing.setDiscountValue(promotion.getDiscountValue());
        existing.setStartTime(promotion.getStartTime());
        existing.setEndTime(promotion.getEndTime());
        existing.setUsageLimit(promotion.getUsageLimit());
        existing.setScopeType(promotion.getScopeType());
        existing.setScopeValue(promotion.getScopeValue());
        existing.setCouponCode(promotion.getCouponCode());

        return promotionRepository.save(existing);
    }

    // Delete Promotion
    public void deletePromotion(UUID id) {
        promotionRepository.deleteById(id);
    }
}