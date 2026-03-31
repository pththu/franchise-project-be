package franchiseproject.promotion_service.service.impl;

import franchiseproject.promotion_service.client.LoyaltyClient;
import franchiseproject.promotion_service.dto.ApplyDiscountRequest;
import franchiseproject.promotion_service.dto.PromotionDiscountResponse;
import franchiseproject.promotion_service.dto.PromotionRequest;
import franchiseproject.promotion_service.dto.PromotionResponse;
import franchiseproject.promotion_service.entity.Promotion;
import franchiseproject.promotion_service.entity.PromotionUsage;
import franchiseproject.promotion_service.entity.UserPromotionUsage;
import franchiseproject.promotion_service.enums.LoyaltyTier;
import franchiseproject.promotion_service.enums.PromotionStatus;
import franchiseproject.promotion_service.enums.PromotionUsageStatus;
import franchiseproject.promotion_service.repository.PromotionRepository;
import franchiseproject.promotion_service.repository.PromotionUsageRepository;
import franchiseproject.promotion_service.repository.UserPromotionUsageRepository;
import franchiseproject.promotion_service.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository repo;
    private final LoyaltyClient loyaltyClient;
    private final PromotionUsageRepository usageRepo;
    private final UserPromotionUsageRepository userPromotionUsageRepo;

    @Override
    public void create(PromotionRequest req) {
        Promotion p = Promotion.builder()
                .name(req.getName())
                .discountType(req.getDiscountType())
                .discountValue(req.getDiscountValue())
                .requiredRank(req.getRequiredRank())
                .usageLimit(req.getUsageLimit())
                .minOrderValue(req.getMinOrderValue())
                .maxDiscountValue(req.getMaxDiscountValue())
                .expiryDate(req.getExpiryDate())
                .status(req.getStatus())
                .perUserLimit(req.getPerUserLimit())
                .build();

        repo.save(p);
    }

    @Override
    public List<Promotion> getAll() {
        return repo.findAll();
    }

    @Override
    public PromotionResponse getById(UUID id) {

        Promotion p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        return PromotionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .discountType(p.getDiscountType())
                .discountValue(p.getDiscountValue())
                .requiredRank(p.getRequiredRank())
                .usageLimit(p.getUsageLimit())
                .usedCount(p.getUsedCount())
                .perUserLimit(p.getPerUserLimit())
                .minOrderValue(p.getMinOrderValue())
                .maxDiscountValue(p.getMaxDiscountValue())
                .expiryDate(p.getExpiryDate())
                .status(p.getStatus())
                .build();
    }

    @Override
    public void update(UUID id, PromotionRequest req) {
        Promotion p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        p.setName(req.getName());
        p.setDiscountType(req.getDiscountType());
        p.setDiscountValue(req.getDiscountValue());
        p.setRequiredRank(req.getRequiredRank());
        p.setUsageLimit(req.getUsageLimit());
        p.setMinOrderValue(req.getMinOrderValue());
        p.setMaxDiscountValue(req.getMaxDiscountValue());
        p.setExpiryDate(req.getExpiryDate());
        p.setStatus(req.getStatus());
        p.setPerUserLimit(req.getPerUserLimit());

        repo.save(p);
    }

    @Override
    public void delete(UUID id) {
        repo.deleteById(id);
    }


    @Override
    public List<Promotion> getAvailablePromotions(UUID customerId, BigDecimal orderValue) {

        String rankStr = loyaltyClient.getRank(customerId);

        if (rankStr == null || rankStr.isBlank()) {
            rankStr = "BRONZE";
        }

        LoyaltyTier rank = LoyaltyTier.valueOf(rankStr);

        return repo.findAll().stream()
                .filter(p -> p.getStatus() == PromotionStatus.ACTIVE)
                .filter(p -> p.getExpiryDate() == null || p.getExpiryDate().isAfter(LocalDateTime.now()))
                .filter(p -> p.getMinOrderValue() == null
                        || orderValue.compareTo(p.getMinOrderValue()) >= 0)
                .filter(p -> p.getRequiredRank() == null
                        || rank.ordinal() >= p.getRequiredRank().ordinal())
                .filter(p -> p.getUsageLimit() == null || p.getUsedCount() < p.getUsageLimit())
                .toList();
    }

    @Override
    public PromotionDiscountResponse applyDiscount(ApplyDiscountRequest req) {

        if (req.getPromotionId() == null) {
            throw new RuntimeException("Không có promotion");
        }

        Promotion p = repo.findById(req.getPromotionId())
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        // 🔥 VALIDATE
        if (p.getStatus() != PromotionStatus.ACTIVE) {
            throw new RuntimeException("Promotion không active");
        }

        if (p.getExpiryDate() != null &&
                p.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Promotion đã hết hạn");
        }

        if (req.getOrderValue().compareTo(p.getMinOrderValue()) < 0) {
            throw new RuntimeException("Không đủ giá trị đơn hàng");
        }

        // 👉 check rank
        String rankStr = loyaltyClient.getRank(req.getCustomerId());
        if (rankStr == null || rankStr.isBlank()) {
            rankStr = "BRONZE";
        }

        LoyaltyTier rank = LoyaltyTier.valueOf(rankStr);

        if (p.getRequiredRank() != null &&
                rank.ordinal() < p.getRequiredRank().ordinal()) {
            throw new RuntimeException("Không đủ rank");
        }

        // 👉 check usage limit
        if (p.getUsageLimit() != null &&
                p.getUsedCount() >= p.getUsageLimit()) {
            throw new RuntimeException("Promotion đã hết lượt");
        }

        if (p.getPerUserLimit() != null) {

            UserPromotionUsage usage = userPromotionUsageRepo
                    .findByUserIdAndPromotionId(req.getCustomerId(), p.getId())
                    .orElse(null);

            int used = (usage == null) ? 0 : usage.getUsedCount();

            if (used >= p.getPerUserLimit()) {
                throw new RuntimeException("Bạn đã dùng hết số lần");
            }
        }

        // 🔥 LƯU PENDING
        PromotionUsage usage = PromotionUsage.builder()
                .id(UUID.randomUUID())
                .promotionId(p.getId())
                .userId(req.getCustomerId())
                .orderId(req.getOrderId())
                .status(PromotionUsageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(2))
                .build();

        usageRepo.save(usage);

        // 🔥 TRẢ VỀ CHO ORDER
        return new PromotionDiscountResponse(
                usage.getId(),
                p.getDiscountValue(),
                p.getMaxDiscountValue(),
                p.getDiscountType()
        );
    }


    @Override
    public void confirmOrder(UUID orderId, String status) {

        PromotionUsage usage = usageRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Usage not found"));

        // ❗ chống duplicate
        if (usage.getStatus() == PromotionUsageStatus.SUCCESS) {
            return;
        }

        if ("PAID".equalsIgnoreCase(status)) {

            Promotion p = repo.findById(usage.getPromotionId())
                    .orElseThrow();

            p.setUsedCount(p.getUsedCount() + 1);
            repo.save(p);

            // 🔥 UPDATE USER USAGE
            UserPromotionUsage userUsage = userPromotionUsageRepo
                    .findByUserIdAndPromotionId(usage.getUserId(), usage.getPromotionId())
                    .orElse(
                            UserPromotionUsage.builder()
                                    .id(UUID.randomUUID())
                                    .userId(usage.getUserId())
                                    .promotionId(usage.getPromotionId())
                                    .usedCount(0)
                                    .createdAt(LocalDateTime.now())
                                    .build()
                    );

            userUsage.setUsedCount(userUsage.getUsedCount() + 1);

            userPromotionUsageRepo.save(userUsage);

            usage.setStatus(PromotionUsageStatus.SUCCESS);
        } else {
            usage.setStatus(PromotionUsageStatus.FAILED);
        }

        usageRepo.save(usage);
    }

    @Scheduled(fixedRate = 60000)
    public void autoExpire() {

        List<PromotionUsage> list = usageRepo
                .findByStatusAndExpiresAtBefore(
                        PromotionUsageStatus.PENDING,
                        LocalDateTime.now()
                );

        for (PromotionUsage u : list) {
            u.setStatus(PromotionUsageStatus.EXPIRED);
        }

        usageRepo.saveAll(list);
    }

    @Scheduled(fixedRate = 60000) // cứ 60s check 1 lần
    public void autoExpirePromotions() {

        List<Promotion> list = repo.findAll().stream()
                .filter(p -> p.getStatus() == PromotionStatus.ACTIVE)
                .filter(p -> p.getExpiryDate() != null && p.getExpiryDate().isBefore(LocalDateTime.now()))
                .toList();

        for (Promotion p : list) {
            p.setStatus(PromotionStatus.EXPIRED);
        }

        repo.saveAll(list);
    }

}