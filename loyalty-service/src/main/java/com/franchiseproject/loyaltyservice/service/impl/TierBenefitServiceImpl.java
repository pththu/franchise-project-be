    package com.franchiseproject.loyaltyservice.service.impl;

    import com.franchiseproject.loyaltyservice.dto.request.ManageTierBenefitRequest;
    import com.franchiseproject.loyaltyservice.dto.response.TierBenefitResponse;
    import com.franchiseproject.loyaltyservice.exception.AppException;
    import com.franchiseproject.loyaltyservice.exception.ErrorCode;
    import com.franchiseproject.loyaltyservice.model.TierBenefit;
    import com.franchiseproject.loyaltyservice.repository.TierBenefitRepository;
    import com.franchiseproject.loyaltyservice.service.TierBenefitService;
    import jakarta.annotation.PostConstruct;
    import lombok.AccessLevel;
    import lombok.AllArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.util.List;
    import java.util.stream.Collectors;

    @Service
    @AllArgsConstructor
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    public class TierBenefitServiceImpl implements TierBenefitService {

        TierBenefitRepository tierBenefitRepository;

        @PostConstruct
        public void initDataToDatabase() {
            if (tierBenefitRepository.count() == 0) {
                tierBenefitRepository.save(new TierBenefit("BRONZE", 0,
                        List.of("Tích lũy điểm mỗi lần mua hàng")));

                tierBenefitRepository.save(new TierBenefit("SILVER", 500,
                        List.of("Giảm 5% cho mọi hóa đơn", "Tặng 1 voucher Free Ship mỗi tháng")));

                tierBenefitRepository.save(new TierBenefit("GOLD", 1000,
                        List.of("Giảm 10% cho mọi hóa đơn", "Tặng bánh kem vào ngày sinh nhật", "Ưu tiên phục vụ không cần xếp hàng")));

                tierBenefitRepository.save(new TierBenefit("PLATINUM", 2000,
                        List.of("Giảm 15% cho mọi hóa đơn", "Tặng 3 voucher Free Ship mỗi tháng")));

                tierBenefitRepository.save(new TierBenefit("DIAMOND", 3000,
                        List.of("Giảm 20% cho mọi hóa đơn", "Có nhân viên chăm sóc riêng")));
            }
        }

        @Override
        public List<TierBenefitResponse> getAllTierBenefits() {
            return tierBenefitRepository.findAll().stream()
                    .map(tier -> new TierBenefitResponse(tier.getTierName(), tier.getRequiredPoints(), tier.getBenefits()))
                    .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void manageTierBenefits(ManageTierBenefitRequest request) {
            String tierName = request.getTierName().toUpperCase().trim();

            TierBenefit tierBenefit = tierBenefitRepository.findById(tierName).orElse(null);

            if (tierBenefit == null) {
                tierBenefit = new TierBenefit(tierName, request.getRequiredPoints(), new java.util.ArrayList<>(request.getBenefits()));
            } else {
                tierBenefit.setRequiredPoints(request.getRequiredPoints());

                if (tierBenefit.getBenefits() != null) {
                    tierBenefit.getBenefits().clear();
                    tierBenefit.getBenefits().addAll(request.getBenefits());
                } else {
                    tierBenefit.setRequiredPoints(request.getRequiredPoints());

                    if (tierBenefit.getBenefits() != null) {
                        tierBenefit.getBenefits().clear();
                        tierBenefit.getBenefits().addAll(request.getBenefits());
                    } else {
                        tierBenefit.setBenefits(new java.util.ArrayList<>(request.getBenefits()));
                    }
                }
            }

            tierBenefitRepository.save(tierBenefit);
        }

        @Override
        @Transactional
        public void deleteTierBenefit(String tierName) {
            String normalizedTierName = tierName.toUpperCase().trim();

            if (!tierBenefitRepository.existsById(normalizedTierName)) {
                throw new AppException(ErrorCode.NOT_FOUND);
            }

            tierBenefitRepository.deleteById(normalizedTierName);
        }
    }