package franchiseproject.promotion_service.service.impl;

import franchiseproject.promotion_service.dto.PromotionResponse;
import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.repository.PromotionRepository;
import franchiseproject.promotion_service.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    // View Promotions
    @Override
    public List<PromotionResponse> getAllPromotions() {

        return promotionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    private PromotionResponse mapToResponse(Promotion promotion) {

        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .discountValue(promotion.getDiscountValue())
                .status(promotion.getStatus().name())
                .updatedAt(promotion.getUpdatedAt())
                .build();

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

        return promotionRepository.save(existing);
    }

    // Delete Promotion
    public void deletePromotion(UUID id) {
        promotionRepository.deleteById(id);
    }
}