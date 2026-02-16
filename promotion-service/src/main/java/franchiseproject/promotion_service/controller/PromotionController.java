package franchiseproject.promotion_service.controller;

import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.service.impl.PromotionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionServiceImpl promotionService;

    @GetMapping
    public List<Promotion> getAllPromotions() {
        return promotionService.getAllPromotions();
    }
}
