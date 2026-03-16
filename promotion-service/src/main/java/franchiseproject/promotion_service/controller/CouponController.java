package franchiseproject.promotion_service.controller;

import franchiseproject.promotion_service.dto.CouponUpdateRequest;
import franchiseproject.promotion_service.repository.CouponRepository;
import franchiseproject.promotion_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CouponRepository couponRepository;

    @PostMapping("/generate/{promotionId}")
    public ResponseEntity<?> generateCoupon(
            @PathVariable UUID promotionId,
            @RequestParam int quantity,
            @RequestParam int usageLimit,
            @RequestParam Instant expiryDate
    ) {
        return ResponseEntity.ok(
                couponService.generateCoupons(promotionId, quantity, usageLimit, expiryDate)
        );
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<?> getCouponsByPromotion(@PathVariable UUID promotionId) {
        return ResponseEntity.ok(couponRepository.findByPromotionId(promotionId));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<?> getCouponDetail(@PathVariable UUID id) {
//        return ResponseEntity.ok(couponService.getCouponDetail(id));
//    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(
            @PathVariable UUID id,
            @RequestBody CouponUpdateRequest request
    ) {

        return ResponseEntity.ok(
                couponService.updateCoupon(id, request)
        );

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable UUID id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok("Coupon deleted");
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateCoupon(@PathVariable String code) {
        return ResponseEntity.ok(couponService.validateCoupon(code));
    }
}
