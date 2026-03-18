package franchiseproject.promotion_service.service.impl;

import franchiseproject.promotion_service.dto.CouponResponse;
import franchiseproject.promotion_service.dto.CouponUpdateRequest;
import franchiseproject.promotion_service.entity.Coupon;
import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.enums.CouponStatus;
import franchiseproject.promotion_service.exception.CouponInvalidException;
import franchiseproject.promotion_service.exception.CouponNotFoundException;
import franchiseproject.promotion_service.repository.CouponRepository;
import franchiseproject.promotion_service.repository.PromotionRepository;
import franchiseproject.promotion_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
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
                .status(CouponStatus.ACTIVE)
                .promotion(promotion)
                .build();

        return couponRepository.save(coupon);
    }

    @Override
    public CouponResponse updateCoupon(UUID id, CouponUpdateRequest request) {

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (request.getUsageLimit() != null) {
            coupon.setUsageLimit(request.getUsageLimit());
        }

        if (request.getExpiryDate() != null) {
            coupon.setExpiryDate(request.getExpiryDate());
        }

        if (request.getStatus() != null) {
            coupon.setStatus(request.getStatus());
        }

        couponRepository.save(coupon);

        return mapToResponse(coupon);
    }

    @Override
    public List<Coupon> generateCoupons(UUID promotionId, int quantity, int usageLimit, Instant expiryDate) {

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        List<Coupon> coupons = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {

            String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Coupon coupon = Coupon.builder()
                    .couponCode(code)
                    .usageLimit(usageLimit)
                    .usedCount(0)
                    .status(CouponStatus.ACTIVE)
                    .expiryDate(expiryDate)
                    .promotion(promotion)
                    .build();

            coupons.add(coupon);
        }

        return couponRepository.saveAll(coupons);
    }

    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon getCouponDetail(UUID id) {

        return couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found"));
    }
    @Override
    public void deleteCoupon(UUID id) {

        Coupon coupon = couponRepository.findById(id)
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

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new CouponInvalidException("Coupon not active");
        }

        if (coupon.getExpiryDate() != null &&
                coupon.getExpiryDate().isBefore(Instant.now())) {

            throw new CouponInvalidException("Coupon expired");
        }

        if (coupon.getUsageLimit() != null &&
                coupon.getUsedCount() >= coupon.getUsageLimit()) {

            throw new CouponInvalidException("Coupon usage limit reached");
        }

        return true;
    }
    private CouponResponse mapToResponse(Coupon coupon) {

        return CouponResponse.builder()
                .id(coupon.getId())
                .couponCode(coupon.getCouponCode())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .expiryDate(coupon.getExpiryDate())
                .status(coupon.getStatus())
                .promotionId(coupon.getPromotion().getId())
                .build();

    }
}
