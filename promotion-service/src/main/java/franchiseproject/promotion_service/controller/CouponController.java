package franchiseproject.promotion_service.controller;

import franchiseproject.promotion_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/generate/{promotionId}")
    public ResponseEntity<?> generateCoupon(@PathVariable UUID promotionId) {
        return ResponseEntity.ok(couponService.generateCoupon(promotionId));
    }

    @GetMapping
    public ResponseEntity<?> getCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCouponDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(couponService.getCouponDetail(id));
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
