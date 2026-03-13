package franchiseproject.promotion_service.service.impl;

import franchiseproject.promotion_service.entity.Coupon;
import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.exception.CouponInvalidException;
import franchiseproject.promotion_service.exception.CouponNotFoundException;
import franchiseproject.promotion_service.repository.CouponRepository;
import franchiseproject.promotion_service.repository.PromotionRepository;
import franchiseproject.promotion_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final PromotionRepository promotionRepository;

    @Override
    public Coupon generateCoupon(UUID promotionId) {

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Coupon coupon = Coupon.builder()
                .couponCode(code)
                .usageLimit(1)
                .usedCount(0)
                .promotion(promotion)
                .build();

        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon getCouponDetail(UUID promotionId) {
        return couponRepository.findByPromotionId(promotionId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found"));
    }
    @Override
    public void deleteCoupon(UUID promotionId) {

        Coupon coupon = couponRepository.findByPromotionId(promotionId)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found"));

        couponRepository.delete(coupon);
    }

    @Override
    public boolean validateCoupon(String couponCode) {

        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new CouponInvalidException("Invalid coupon code"));

        Promotion promotion = coupon.getPromotion();

        if (promotion.getStartTime().isAfter(Instant.now())) {
            throw new CouponInvalidException("Promotion not started");
        }

        if (promotion.getEndTime().isBefore(Instant.now())) {
            throw new CouponInvalidException("Promotion expired");
        }

        if (coupon.getUsageLimit() != null &&
                coupon.getUsedCount() >= coupon.getUsageLimit()) {

            throw new CouponInvalidException("Coupon usage limit reached");
        }

        return true;
    }
}
