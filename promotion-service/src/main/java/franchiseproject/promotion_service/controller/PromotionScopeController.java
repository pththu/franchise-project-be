package franchiseproject.promotion_service.controller;

import franchiseproject.promotion_service.dto.CreatePromotionScopeRequest;
import franchiseproject.promotion_service.entity.PromotionScope;
import franchiseproject.promotion_service.service.PromotionScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions/scopes")
@RequiredArgsConstructor
public class PromotionScopeController {

    private final PromotionScopeService promotionScopeService;

    @PostMapping
    public PromotionScope createScope(@RequestBody CreatePromotionScopeRequest request) {
        return promotionScopeService.createScope(request);
    }

    @PutMapping("/{id}")
    public PromotionScope updateScope(@PathVariable UUID id,
                                      @RequestBody CreatePromotionScopeRequest request) {
        return promotionScopeService.updateScope(id, request);
    }

    @GetMapping("/{promotionId}")
    public List<PromotionScope> getScopes(@PathVariable UUID promotionId) {
        return promotionScopeService.getScopes(promotionId);
    }
}