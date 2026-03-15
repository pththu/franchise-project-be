package franchiseproject.promotion_service.controller;

import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.service.impl.PromotionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionServiceImpl promotionService;

    // View Promotions
    @GetMapping
    public List<Promotion> getAllPromotions() {
        return promotionService.getAllPromotions();
    }

    // View Promotion Details
    @GetMapping("/{id}")
    public Promotion getPromotionById(@PathVariable UUID id) {
        return promotionService.getPromotionById(id);
    }

    // Create Promotion
    @PostMapping
    public Promotion createPromotion(@RequestBody Promotion promotion) {
        return promotionService.createPromotion(promotion);
    }

    // Update Promotion
    @PutMapping("/{id}")
    public Promotion updatePromotion(
            @PathVariable UUID id,
            @RequestBody Promotion promotion
    ) {
        return promotionService.updatePromotion(id, promotion);
    }

    // Delete Promotion
    @DeleteMapping("/{id}")
    public void deletePromotion(@PathVariable UUID id) {
        promotionService.deletePromotion(id);
    }
}