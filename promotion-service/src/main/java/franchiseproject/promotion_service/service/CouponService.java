package franchiseproject.promotion_service.service;

import franchiseproject.promotion_service.dto.CouponResponse;
import franchiseproject.promotion_service.dto.CouponUpdateRequest;
import franchiseproject.promotion_service.entity.Coupon;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CouponService {

    Coupon generateCoupon(UUID promotionId);

    List<Coupon> getAllCoupons();

    Coupon getCouponDetail(UUID id);

    void deleteCoupon(UUID id);

    boolean validateCoupon(String couponCode);

    List<Coupon> generateCoupons(UUID promotionId, int quantity, int usageLimit, Instant expiryDate);

    CouponResponse updateCoupon(UUID id, CouponUpdateRequest request);
}