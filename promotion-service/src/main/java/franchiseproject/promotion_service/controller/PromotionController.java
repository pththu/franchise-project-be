package franchiseproject.promotion_service.controller;

import franchiseproject.promotion_service.dto.ApplyDiscountRequest;
import franchiseproject.promotion_service.dto.PromotionRequest;
import franchiseproject.promotion_service.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService service;

    @PostMapping
    public void create(@RequestBody PromotionRequest req) {
        service.create(req);
    }

    @GetMapping
    public Object getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Object getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable UUID id, @RequestBody PromotionRequest req) {
        service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    // 🔥 lấy promotion user dùng được
    @GetMapping("/available")
    public Object available(
            @RequestParam UUID userId,
            @RequestParam BigDecimal orderValue
    ) {
        return service.getAvailablePromotions(userId, orderValue);
    }
    @PostMapping("/trace")
    public void confirm(
            @RequestParam UUID orderId,
            @RequestParam String status
    ){
        service.confirmOrder(orderId, status);
    }

    // 🔥 APPLY DISCOUNT
    @PostMapping("/reserve")
    public Object apply(@RequestBody ApplyDiscountRequest req) {
        return service.applyDiscount(req);
    }
}